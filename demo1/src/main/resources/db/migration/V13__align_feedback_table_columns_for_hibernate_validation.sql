-- ============================================
-- Flyway Migration: V13__align_feedback_table_columns_for_hibernate_validation.sql
-- 目标：补齐 t_feedback 与当前 JPA 实体校验所需列
-- ============================================

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS user_id UUID;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS teacher_id UUID;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS qa_history_id UUID;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS rating INT;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS comment TEXT;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS feedback_type VARCHAR(20) DEFAULT 'OTHER';

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS reply_content TEXT;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS replied_at TIMESTAMP;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

UPDATE t_feedback
SET feedback_type = 'OTHER'
WHERE feedback_type IS NULL;

UPDATE t_feedback
SET status = 'PENDING'
WHERE status IS NULL;

CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON t_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_teacher_id ON t_feedback(teacher_id);
CREATE INDEX IF NOT EXISTS idx_feedback_qa_history_id ON t_feedback(qa_history_id);
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON t_feedback(created_at DESC);
