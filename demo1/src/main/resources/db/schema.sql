-- ============================================
-- RAG 教学知识库问答系统 - 完整数据库 Schema
-- 数据库: PostgreSQL 15+ with pgvector
-- 版本: v3.0
-- 更新日期: 2026-02-03
-- ============================================

-- ============================================
-- 第1部分: 创建扩展
-- ============================================

-- 创建必要的扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 第2部分: 创建表结构
-- ============================================

-- --------------------------------------------
-- 表 1: t_user (用户表)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    password VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE(username),
    CONSTRAINT uk_user_email UNIQUE(email),
    CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'))
);

COMMENT ON TABLE t_user IS '用户表 - 存储系统用户信息';
COMMENT ON COLUMN t_user.id IS '用户唯一标识UUID';
COMMENT ON COLUMN t_user.username IS '用户名，唯一';
COMMENT ON COLUMN t_user.email IS '邮箱地址，唯一';
COMMENT ON COLUMN t_user.password IS '密码，BCrypt加密存储';
COMMENT ON COLUMN t_user.role IS '用户角色: STUDENT-学生, TEACHER-教师, ADMIN-管理员';
COMMENT ON COLUMN t_user.is_active IS '账户是否激活';

-- --------------------------------------------
-- 表 2: t_document (文档表)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS t_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    file_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'UPLOADING',
    error_message TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    chunk_count INT DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_document_user_filename UNIQUE(user_id, filename),
    CONSTRAINT chk_document_status CHECK (status IN ('UPLOADING', 'PROCESSING', 'SUCCESS', 'FAILED')),
    CONSTRAINT chk_document_file_type CHECK (file_type IN ('PDF', 'DOCX', 'PPTX', 'TXT', 'UNKNOWN'))
);

COMMENT ON TABLE t_document IS '文档表 - 存储上传的文档元数据';
COMMENT ON COLUMN t_document.status IS '文档处理状态: UPLOADING-上传中, PROCESSING-处理中, SUCCESS-成功, FAILED-失败';
COMMENT ON COLUMN t_document.file_type IS '文件类型: PDF, DOCX, PPTX, TXT';
COMMENT ON COLUMN t_document.chunk_count IS '文档分块数量';

-- --------------------------------------------
-- 表 3: t_document_chunk (文档分块表)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS t_document_chunk (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES t_document(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_length INT,
    page_num INT,
    section_title VARCHAR(255),
    char_start INT,
    char_end INT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chunk_document_index UNIQUE(document_id, chunk_index),
    CONSTRAINT chk_chunk_index CHECK (chunk_index >= 0),
    CONSTRAINT chk_content_length CHECK (content_length >= 0)
);

COMMENT ON TABLE t_document_chunk IS '文档分块表 - 存储文档切分后的文本块';
COMMENT ON COLUMN t_document_chunk.chunk_index IS '分块在文档中的序号，从0开始';
COMMENT ON COLUMN t_document_chunk.content IS '分块的文本内容';
COMMENT ON COLUMN t_document_chunk.page_num IS '分块所在的页码';
COMMENT ON COLUMN t_document_chunk.metadata IS 'JSON格式的额外元数据';

-- --------------------------------------------
-- 表 4: t_vector_record (向量记录表)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS t_vector_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chunk_id UUID NOT NULL REFERENCES t_document_chunk(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES t_document(id) ON DELETE CASCADE,
    embedding vector(1024),
    embedding_dim INT DEFAULT 1024,
    embedding_model VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vector_chunk UNIQUE(chunk_id),
    CONSTRAINT chk_embedding_dim CHECK (embedding_dim > 0)
);

COMMENT ON TABLE t_vector_record IS '向量记录表 - 存储文档分块的向量表示，用于语义检索';
COMMENT ON COLUMN t_vector_record.embedding IS '文本的向量表示，维度为1024';
COMMENT ON COLUMN t_vector_record.embedding_dim IS '向量维度';
COMMENT ON COLUMN t_vector_record.embedding_model IS '生成向量的模型名称';

-- --------------------------------------------
-- 表 5: t_qa_history (问答历史表)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS t_qa_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    question VARCHAR(1000) NOT NULL,
    answer TEXT NOT NULL,
    response_time INT,
    retrieved_chunks TEXT,
    retrieved_documents TEXT,
    model_version VARCHAR(100),
    asked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_response_time CHECK (response_time >= 0)
);

COMMENT ON TABLE t_qa_history IS '问答历史表 - 记录用户的提问和系统回答';
COMMENT ON COLUMN t_qa_history.question IS '用户提出的问题';
COMMENT ON COLUMN t_qa_history.answer IS '系统生成的答案';
COMMENT ON COLUMN t_qa_history.response_time IS '响应时间（毫秒）';
COMMENT ON COLUMN t_qa_history.model_version IS '使用的LLM模型版本';

-- --------------------------------------------
-- 表 6: t_citation (引用来源表)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS t_citation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    qa_id UUID NOT NULL REFERENCES t_qa_history(id) ON DELETE CASCADE,
    chunk_id UUID NOT NULL REFERENCES t_document_chunk(id),
    document_id UUID NOT NULL REFERENCES t_document(id),
    page_num INT,
    chunk_index INT,
    relevance_score FLOAT,
    citation_text VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_citation_score CHECK (relevance_score >= 0 AND relevance_score <= 1),
    CONSTRAINT chk_citation_chunk_index CHECK (chunk_index >= 0)
);

COMMENT ON TABLE t_citation IS '引用来源表 - 记录答案引用的文档来源';
COMMENT ON COLUMN t_citation.relevance_score IS '相关度分数，范围0-1';
COMMENT ON COLUMN t_citation.citation_text IS '引用的文本片段';

-- ============================================
-- 第3部分: 创建索引
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
-- 第4部分: 创建触发器函数和触发器
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
-- 第5部分: 创建实用函数
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

-- ============================================
-- 第6部分: 插入初始数据
-- ============================================

-- 插入初始账号
-- 密码使用 BCrypt 加密: 123456
-- BCrypt哈希: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO

-- 学生账号
INSERT INTO t_user (id, username, email, password, role, is_active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'student', 'student@example.com',
     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',
     'STUDENT', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 教师账号
INSERT INTO t_user (id, username, email, password, role, is_active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000002', 'teacher', 'teacher@example.com',
     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',
     'TEACHER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 管理员账号
INSERT INTO t_user (id, username, email, password, role, is_active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000003', 'admin', 'admin@example.com',
     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',
     'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- ============================================
-- 第7部分: 验证脚本
-- ============================================

-- 检查扩展
SELECT extname, extversion FROM pg_extension WHERE extname IN ('vector', 'uuid-ossp');

-- 检查表
SELECT table_name,
       (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- 检查索引
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- 检查初始用户
SELECT username, email, role, is_active, created_at FROM t_user;

-- ============================================
-- 初始化完成
-- ============================================
\echo '=========================================='
\echo 'RAG QA System 数据库 Schema 创建完成！'
\echo '=========================================='
\echo '数据库名称: rag_qa_system'
\echo '扩展: vector, uuid-ossp'
\echo '表数量: 6'
\echo '初始账号: student/teacher/admin (密码: 123456)'
\echo '=========================================='
