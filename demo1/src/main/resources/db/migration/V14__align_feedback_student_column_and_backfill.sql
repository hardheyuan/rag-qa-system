-- ============================================
-- Flyway Migration: V14__align_feedback_student_column_and_backfill.sql
-- 目标：统一学生外键列为 student_id，兼容历史 user_id 数据
-- ============================================

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS student_id UUID;

UPDATE t_feedback
SET student_id = user_id
WHERE student_id IS NULL
  AND user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_feedback_student_id ON t_feedback(student_id);
