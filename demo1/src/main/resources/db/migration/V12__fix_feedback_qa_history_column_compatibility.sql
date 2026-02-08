-- ============================================
-- Flyway Migration: V12__fix_feedback_qa_history_column_compatibility.sql
-- 目标：兼容历史库中 t_feedback 缺少 qa_history_id 的情况
-- ============================================

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS qa_history_id UUID;

CREATE INDEX IF NOT EXISTS idx_feedback_qa_history_id ON t_feedback(qa_history_id);
