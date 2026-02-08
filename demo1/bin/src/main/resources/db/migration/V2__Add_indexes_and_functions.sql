-- ============================================
-- Flyway Migration: V2__Add_indexes_and_functions.sql
-- 添加索引、触发器和实用函数
-- ============================================

-- ============================================
-- 创建索引
-- ============================================

-- t_user 表索引
CREATE INDEX IF NOT EXISTS idx_user_username ON t_user(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON t_user(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON t_user(role);
CREATE INDEX IF NOT EXISTS idx_user_active ON t_user(is_active);
CREATE INDEX IF NOT EXISTS idx_user_created_at ON t_user(created_at DESC);

-- t_document 表索引
CREATE INDEX IF NOT EXISTS idx_document_user_id ON t_document(user_id);
CREATE INDEX IF NOT EXISTS idx_document_status ON t_document(status);
CREATE INDEX IF NOT EXISTS idx_document_file_type ON t_document(file_type);
CREATE INDEX IF NOT EXISTS idx_document_created_at ON t_document(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_document_uploaded_at ON t_document(uploaded_at DESC);

-- t_document_chunk 表索引
CREATE INDEX IF NOT EXISTS idx_chunk_document_id ON t_document_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_page_num ON t_document_chunk(document_id, page_num);
CREATE INDEX IF NOT EXISTS idx_chunk_index ON t_document_chunk(chunk_index);
CREATE INDEX IF NOT EXISTS idx_chunk_metadata ON t_document_chunk USING gin(metadata);

-- t_vector_record 表索引
CREATE INDEX IF NOT EXISTS idx_vector_document_id ON t_vector_record(document_id);
CREATE INDEX IF NOT EXISTS idx_vector_chunk_id ON t_vector_record(chunk_id);
CREATE INDEX IF NOT EXISTS idx_vector_model ON t_vector_record(embedding_model);

-- HNSW 向量相似度搜索索引（生产环境推荐）
-- 注意: 需要确保向量数据已存在后再创建
CREATE INDEX IF NOT EXISTS idx_vector_embedding_cosine ON t_vector_record
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

-- t_qa_history 表索引
CREATE INDEX IF NOT EXISTS idx_qa_user_id ON t_qa_history(user_id);
CREATE INDEX IF NOT EXISTS idx_qa_asked_at ON t_qa_history(asked_at DESC);
CREATE INDEX IF NOT EXISTS idx_qa_created_at ON t_qa_history(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_qa_model_version ON t_qa_history(model_version);

-- t_citation 表索引
CREATE INDEX IF NOT EXISTS idx_citation_qa_id ON t_citation(qa_id);
CREATE INDEX IF NOT EXISTS idx_citation_chunk_id ON t_citation(chunk_id);
CREATE INDEX IF NOT EXISTS idx_citation_document_id ON t_citation(document_id);
CREATE INDEX IF NOT EXISTS idx_citation_relevance_score ON t_citation(relevance_score DESC);

-- ============================================
-- 创建触发器
-- ============================================

-- 自动更新 updated_at 字段的触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为需要的表添加触发器
DROP TRIGGER IF EXISTS update_user_updated_at ON t_user;
CREATE TRIGGER update_user_updated_at BEFORE UPDATE ON t_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_document_updated_at ON t_document;
CREATE TRIGGER update_document_updated_at BEFORE UPDATE ON t_document
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 创建实用函数
-- ============================================

-- 向量相似度搜索函数
CREATE OR REPLACE FUNCTION search_similar_vectors(
    query_embedding vector(1024),
    user_id_param UUID,
    top_k INT DEFAULT 5
)
RETURNS TABLE (
    chunk_id UUID,
    document_id UUID,
    content TEXT,
    filename VARCHAR,
    page_num INT,
    similarity_score FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        dc.id as chunk_id,
        d.id as document_id,
        dc.content,
        d.filename,
        dc.page_num,
        1 - (vr.embedding <=> query_embedding) as similarity_score
    FROM t_vector_record vr
    JOIN t_document_chunk dc ON vr.chunk_id = dc.id
    JOIN t_document d ON vr.document_id = d.id
    WHERE d.user_id = user_id_param
      AND d.status = 'SUCCESS'
    ORDER BY vr.embedding <=> query_embedding
    LIMIT top_k;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION search_similar_vectors IS '向量相似度搜索函数 - 查找最相似的文档分块';

-- 清理孤立数据函数
CREATE OR REPLACE FUNCTION cleanup_orphaned_data()
RETURNS TABLE (
    deleted_chunks INT,
    deleted_vectors INT
) AS $$
DECLARE
    chunks_deleted INT;
    vectors_deleted INT;
BEGIN
    -- 删除没有向量的分块（超过1小时）
    DELETE FROM t_document_chunk dc
    WHERE NOT EXISTS (
        SELECT 1 FROM t_vector_record vr WHERE vr.chunk_id = dc.id
    )
    AND dc.created_at < NOW() - INTERVAL '1 hour';

    GET DIAGNOSTICS chunks_deleted = ROW_COUNT;

    -- 删除文档已删除的孤立向量
    DELETE FROM t_vector_record vr
    WHERE NOT EXISTS (
        SELECT 1 FROM t_document d WHERE d.id = vr.document_id
    );

    GET DIAGNOSTICS vectors_deleted = ROW_COUNT;

    RETURN QUERY SELECT chunks_deleted, vectors_deleted;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_orphaned_data IS '清理孤立数据函数 - 删除没有向量的分块和孤立向量';
