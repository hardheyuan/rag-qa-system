-- ============================================
-- Flyway Migration: V11__fix_feedback_comment_column_compatibility.sql
-- 目标：兼容历史库中 t_feedback 只有 content 无 comment 的情况
-- ============================================

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS comment TEXT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 't_feedback'
          AND column_name = 'content'
    ) THEN
        UPDATE t_feedback
        SET comment = content
        WHERE comment IS NULL
          AND content IS NOT NULL;
    END IF;
END $$;
