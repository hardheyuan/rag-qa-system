-- ============================================
-- Flyway Migration: V1__Initial_schema.sql
-- 初始数据库结构创建
-- ============================================

-- 创建必要的扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 创建表结构
-- ============================================

-- t_user (用户表)
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
COMMENT ON COLUMN t_user.role IS '用户角色: STUDENT-学生, TEACHER-教师, ADMIN-管理员';

-- t_document (文档表)
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

-- t_document_chunk (文档分块表)
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

-- t_vector_record (向量记录表)
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

-- t_qa_history (问答历史表)
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

-- t_citation (引用来源表)
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
