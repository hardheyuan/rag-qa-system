-- JWT 认证系统数据库迁移脚本
-- 添加用户角色字段并创建初始账号
-- 执行时间: 2024-01-01

-- =====================================================
-- 第1步: 添加角色字段到用户表
-- =====================================================

-- 检查并添加 role 字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 't_user' AND column_name = 'role'
    ) THEN
        ALTER TABLE t_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'STUDENT';
        
        -- 添加约束确保只能是有效角色
        ALTER TABLE t_user ADD CONSTRAINT chk_user_role 
            CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));
        
        RAISE NOTICE 'Added role column to t_user table';
    ELSE
        RAISE NOTICE 'Role column already exists';
    END IF;
END $$;

-- =====================================================
-- 第2步: 创建初始账号
-- 密码使用 BCrypt 加密: 123456
-- BCrypt哈希: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO
-- =====================================================

-- 删除可能已存在的初始账号（用于重复执行脚本）
DELETE FROM t_user WHERE username IN ('student', 'teacher', 'admin');

-- 学生账号
INSERT INTO t_user (id, username, email, password, role, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(), 
    'student', 
    'student@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- 密码: 123456
    'STUDENT',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'student');

-- 教师账号
INSERT INTO t_user (id, username, email, password, role, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'teacher',
    'teacher@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- 密码: 123456
    'TEACHER',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'teacher');

-- 管理员账号
INSERT INTO t_user (id, username, email, password, role, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'admin',
    'admin@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- 密码: 123456
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'admin');

-- =====================================================
-- 第3步: 添加索引优化查询性能
-- =====================================================

-- 为角色字段添加索引（常用于权限查询）
CREATE INDEX IF NOT EXISTS idx_user_role ON t_user(role);

-- 为激活状态添加索引（常用于筛选活跃用户）
CREATE INDEX IF NOT EXISTS idx_user_active ON t_user(is_active);

-- =====================================================
-- 第4步: 验证初始账号创建
-- =====================================================

DO $$
DECLARE
    student_count INT;
    teacher_count INT;
    admin_count INT;
BEGIN
    SELECT COUNT(*) INTO student_count FROM t_user WHERE username = 'student';
    SELECT COUNT(*) INTO teacher_count FROM t_user WHERE username = 'teacher';
    SELECT COUNT(*) INTO admin_count FROM t_user WHERE username = 'admin';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Initial accounts created successfully:';
    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'Student account: student/123456 (%)', CASE WHEN student_count > 0 THEN 'OK' ELSE 'FAILED' END;
    RAISE NOTICE 'Teacher account: teacher/123456 (%)', CASE WHEN teacher_count > 0 THEN 'OK' ELSE 'FAILED' END;
    RAISE NOTICE 'Admin account:   admin/123456 (%)', CASE WHEN admin_count > 0 THEN 'OK' ELSE 'FAILED' END;
    RAISE NOTICE '========================================';
END $$;

-- =====================================================
-- 使用说明
-- =====================================================
-- 初始账号信息：
-- 1. 学生: username=student, password=123456
-- 2. 教师: username=teacher, password=123456
-- 3. 管理员: username=admin, password=123456
--
-- 执行此脚本：
-- psql -U postgres -d rag_qa_system -f jwt_migration.sql
-- 
-- 或者在Spring Boot启动时自动执行（配置spring.sql.init.mode=always）
-- =====================================================
