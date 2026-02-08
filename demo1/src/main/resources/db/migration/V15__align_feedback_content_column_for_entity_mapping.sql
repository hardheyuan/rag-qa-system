-- ============================================
-- Flyway Migration: V15__align_feedback_content_column_for_entity_mapping.sql
-- 目标：统一反馈正文列为 content，兼容历史 comment 列数据
-- ============================================

ALTER TABLE IF EXISTS t_feedback
    ADD COLUMN IF NOT EXISTS content TEXT;

-- 历史库可能只有 comment 列，先回填到 content
UPDATE t_feedback
SET content = comment
WHERE content IS NULL
  AND comment IS NOT NULL;

-- 防止历史脏数据导致后续插入失败
UPDATE t_feedback
SET content = ''
WHERE content IS NULL;

ALTER TABLE IF EXISTS t_feedback
    ALTER COLUMN content SET NOT NULL;
