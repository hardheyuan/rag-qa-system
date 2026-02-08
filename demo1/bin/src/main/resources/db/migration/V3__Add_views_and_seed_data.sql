-- ============================================
-- Flyway Migration: V3__Add_views_and_seed_data.sql
-- 创建视图和初始数据
-- ============================================

-- ============================================
-- 创建视图
-- ============================================

-- 视图1: 文档统计视图
CREATE OR REPLACE VIEW v_document_stats AS
SELECT
    d.id,
    d.filename,
    d.file_type,
    d.status,
    d.chunk_count,
    d.file_size,
    d.created_at,
    u.username,
    COUNT(DISTINCT dc.id) as actual_chunk_count,
    COUNT(DISTINCT vr.id) as vector_count
FROM t_document d
LEFT JOIN t_user u ON d.user_id = u.id
LEFT JOIN t_document_chunk dc ON d.id = dc.document_id
LEFT JOIN t_vector_record vr ON d.id = vr.document_id
GROUP BY d.id, d.filename, d.file_type, d.status, d.chunk_count, d.file_size, d.created_at, u.username;

COMMENT ON VIEW v_document_stats IS '文档统计视图 - 显示文档的详细统计信息';

-- 视图2: 用户问答统计视图
CREATE OR REPLACE VIEW v_user_qa_stats AS
SELECT
    u.id as user_id,
    u.username,
    COUNT(qa.id) as total_questions,
    AVG(qa.response_time) as avg_response_time,
    MAX(qa.asked_at) as last_question_time,
    COUNT(DISTINCT DATE(qa.asked_at)) as active_days
FROM t_user u
LEFT JOIN t_qa_history qa ON u.id = qa.user_id
GROUP BY u.id, u.username;

COMMENT ON VIEW v_user_qa_stats IS '用户问答统计视图 - 显示用户的问答活动统计';

-- ============================================
-- 插入初始数据
-- ============================================

-- 插入演示用户
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
