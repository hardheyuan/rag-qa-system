-- ============================================
-- Flyway Migration: V9__Align_schema_for_supabase_and_entity_consistency.sql
-- 目标:
-- 1) 补齐实体需要的 updated_at 字段
-- 2) 确保 t_user.role 结构在所有历史库中一致
-- ============================================

-- 补齐 BaseEntity 对应的 updated_at（历史脚本缺失）
ALTER TABLE t_document_chunk ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_vector_record ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_qa_history ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_citation ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 统一 role 字段和约束（防止历史库状态不一致）
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS role VARCHAR(20);

UPDATE t_user
SET role = 'STUDENT'
WHERE role IS NULL
   OR role = ''
   OR role NOT IN ('STUDENT', 'TEACHER', 'ADMIN');

ALTER TABLE t_user ALTER COLUMN role SET DEFAULT 'STUDENT';
ALTER TABLE t_user ALTER COLUMN role SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_user_role'
          AND conrelid = 't_user'::regclass
    ) THEN
        ALTER TABLE t_user
            ADD CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_user_role ON t_user(role);
