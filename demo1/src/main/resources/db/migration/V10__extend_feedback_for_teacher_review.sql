-- ============================================
-- Flyway Migration: V10__extend_feedback_for_teacher_review.sql
-- 目标：支持学生反馈指定教师、教师待审核与回复
-- ============================================

ALTER TABLE t_feedback
    ADD COLUMN IF NOT EXISTS teacher_id UUID REFERENCES t_user(id) ON DELETE SET NULL;

ALTER TABLE t_feedback
    ADD COLUMN IF NOT EXISTS feedback_type VARCHAR(20) DEFAULT 'OTHER';

ALTER TABLE t_feedback
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';

ALTER TABLE t_feedback
    ADD COLUMN IF NOT EXISTS reply_content TEXT;

ALTER TABLE t_feedback
    ADD COLUMN IF NOT EXISTS replied_at TIMESTAMP;

ALTER TABLE t_feedback
    ADD COLUMN IF NOT EXISTS rating INT;

ALTER TABLE t_feedback
    ALTER COLUMN rating DROP NOT NULL;

ALTER TABLE t_feedback
    DROP CONSTRAINT IF EXISTS chk_feedback_rating;

ALTER TABLE t_feedback
    ADD CONSTRAINT chk_feedback_rating CHECK (rating IS NULL OR rating IN (-1, 1));

UPDATE t_feedback
SET feedback_type = 'OTHER'
WHERE feedback_type IS NULL;

UPDATE t_feedback
SET status = 'PENDING'
WHERE status IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_feedback_type'
          AND conrelid = 't_feedback'::regclass
    ) THEN
        ALTER TABLE t_feedback
            ADD CONSTRAINT chk_feedback_type CHECK (feedback_type IN ('SUGGESTION', 'ISSUE', 'OTHER'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_feedback_status'
          AND conrelid = 't_feedback'::regclass
    ) THEN
        ALTER TABLE t_feedback
            ADD CONSTRAINT chk_feedback_status CHECK (status IN ('PENDING', 'REPLIED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_feedback_teacher_id ON t_feedback(teacher_id);
CREATE INDEX IF NOT EXISTS idx_feedback_teacher_status_created_at ON t_feedback(teacher_id, status, created_at DESC);
