-- ============================================
-- RAG 教学知识库问答系统 - 数据库初始化脚本
-- 数据库: PostgreSQL 15+ with pgvector
-- ============================================

-- 创建必要的扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 表 1: t_user (用户表)
-- ============================================
CREATE TABLE IF NOT EXISTS t_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    password VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE(username),
    CONSTRAINT uk_user_email UNIQUE(email)
);

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_user_username ON t_user(username);

-- ============================================
-- 表 2: t_document (文档表)
-- ============================================
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
    CONSTRAINT uk_document_user_filename UNIQUE(user_id, filename)
);

-- 文档表索引
CREATE INDEX IF NOT EXISTS idx_document_user_id ON t_document(user_id);
CREATE INDEX IF NOT EXISTS idx_document_status ON t_document(status);
CREATE INDEX IF NOT EXISTS idx_document_created_at ON t_document(created_at DESC);

-- ============================================
-- 表 3: t_document_chunk (文本分块表)
-- ============================================
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
    CONSTRAINT uk_chunk_document_index UNIQUE(document_id, chunk_index)
);

-- 分块表索引
CREATE INDEX IF NOT EXISTS idx_chunk_document_id ON t_document_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_page_num ON t_document_chunk(document_id, page_num);

-- ============================================
-- 表 4: t_vector_record (向量记录表 - 核心！)
-- ============================================
CREATE TABLE IF NOT EXISTS t_vector_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chunk_id UUID NOT NULL REFERENCES t_document_chunk(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES t_document(id) ON DELETE CASCADE,
    embedding vector(4096),          -- 向量数据，使用 pgvector 类型
    embedding_dim INT DEFAULT 4096,  -- 向量维度
    embedding_model VARCHAR(100),   -- 模型名称
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vector_chunk UNIQUE(chunk_id)
);

-- 向量表索引
CREATE INDEX IF NOT EXISTS idx_vector_document_id ON t_vector_record(document_id);
-- 可选：生产环境可添加 HNSW 索引以加速向量搜索
-- CREATE INDEX IF NOT EXISTS idx_vector_embedding ON t_vector_record USING hnsw (embedding vector_cosine_ops);

-- ============================================
-- 表 5: t_qa_history (问答历史表)
-- ============================================
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 问答历史表索引
CREATE INDEX IF NOT EXISTS idx_qa_user_id ON t_qa_history(user_id);
CREATE INDEX IF NOT EXISTS idx_qa_asked_at ON t_qa_history(asked_at DESC);

-- ============================================
-- 表 6: t_citation (引用来源表)
-- ============================================
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
    CONSTRAINT chk_citation_score CHECK (relevance_score >= 0 AND relevance_score <= 1)
);

-- 引用表索引
CREATE INDEX IF NOT EXISTS idx_citation_qa_id ON t_citation(qa_id);
CREATE INDEX IF NOT EXISTS idx_citation_chunk_id ON t_citation(chunk_id);

-- ============================================
-- 插入演示用户
-- ============================================
INSERT INTO t_user (username, email) 
VALUES ('user_demo', 'demo@example.com')
ON CONFLICT (username) DO NOTHING;

-- ============================================
-- 验证脚本
-- ============================================
-- 执行以下查询验证表是否创建成功:
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
-- SELECT COUNT(*) FROM t_user;

SELECT extname FROM pg_extension;

