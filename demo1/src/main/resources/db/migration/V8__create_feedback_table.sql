-- ============================================
-- Flyway Migration: V8__create_feedback_table.sql
-- 创建用户反馈表
-- ============================================

CREATE TABLE IF NOT EXISTS t_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES t_user(id) ON DELETE SET NULL,
    qa_history_id UUID REFERENCES t_qa_history(id) ON DELETE SET NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_feedback_rating CHECK (rating IN (-1, 1))
);

CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON t_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_qa_history_id ON t_feedback(qa_history_id);
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON t_feedback(created_at DESC);
