-- 修复历史数据：早期版本错误地按 username=UUID 字符串创建了“影子用户”并挂载文档
-- 目标：将这些文档归属修正到真实登录用户（真实用户 id = 影子用户.username）

WITH legacy_user_mapping AS (
    SELECT legacy.id AS legacy_user_id,
           real.id   AS real_user_id
    FROM t_user legacy
    JOIN t_user real ON legacy.username = real.id::text
    WHERE legacy.id <> real.id
)
UPDATE t_document d
SET user_id = m.real_user_id
FROM legacy_user_mapping m
WHERE d.user_id = m.legacy_user_id
  -- 避免触发 (user_id, filename) 唯一约束冲突
  AND NOT EXISTS (
      SELECT 1
      FROM t_document d2
      WHERE d2.user_id = m.real_user_id
        AND d2.filename = d.filename
        AND d2.id <> d.id
  );
