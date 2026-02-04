-- ============================================
-- Flyway Migration: V4__Fix_existing_data.sql
-- 修复现有数据问题（从旧版本升级时使用）
-- ============================================

-- ============================================
-- 第1部分: 修复向量维度不一致问题
-- ============================================

-- 检查并修复向量维度
DO $$
BEGIN
    -- 检查当前向量维度
    IF EXISTS (
        SELECT 1 FROM t_vector_record LIMIT 1
    ) THEN
        RAISE NOTICE '发现现有向量数据，检查维度...';

        -- 检查是否有非1024维度的向量
        IF EXISTS (
            SELECT 1 FROM t_vector_record
            WHERE embedding_dim != 1024 OR embedding_dim IS NULL
        ) THEN
            RAISE NOTICE '发现向量维度不一致，更新为元数据...';

            -- 更新维度字段
            UPDATE t_vector_record
            SET embedding_dim = 1024
            WHERE embedding_dim IS NULL;

            RAISE NOTICE '向量维度修复完成';
        ELSE
            RAISE NOTICE '所有向量维度一致 (1024维)';
        END IF;
    ELSE
        RAISE NOTICE '暂无向量数据，维度检查跳过';
    END IF;
END $$;

-- ============================================
-- 第2部分: 确保role字段存在
-- ============================================

DO $$
BEGIN
    -- 检查role字段是否存在
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_user' AND column_name = 'role'
    ) THEN
        RAISE NOTICE 't_user表缺少role字段，正在添加...';

        -- 添加role字段
        ALTER TABLE t_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'STUDENT';

        -- 添加约束
        ALTER TABLE t_user ADD CONSTRAINT chk_user_role
            CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));

        -- 为角色字段添加索引
        CREATE INDEX IF NOT EXISTS idx_user_role ON t_user(role);

        RAISE NOTICE 'role字段添加完成';
    ELSE
        RAISE NOTICE 'role字段已存在，检查约束...';

        -- 检查约束是否存在
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.constraint_column_usage
            WHERE table_name = 't_user' AND constraint_name = 'chk_user_role'
        ) THEN
            -- 尝试添加约束（如果数据允许）
            BEGIN
                ALTER TABLE t_user ADD CONSTRAINT chk_user_role
                    CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));
                RAISE NOTICE 'role约束添加成功';
            EXCEPTION WHEN OTHERS THEN
                RAISE NOTICE '无法添加role约束，可能存在无效数据';
            END;
        END IF;
    END IF;
END $$;

-- ============================================
-- 第3部分: 修复现有用户数据
-- ============================================

-- 为现有用户设置默认角色（如果role字段为NULL）
UPDATE t_user
SET role = 'STUDENT'
WHERE role IS NULL OR role = '';

-- ============================================
-- 第4部分: 验证修复结果
-- ============================================

\echo '\n==========================================='
\echo '修复结果验证'
\echo '==========================================='

-- 验证role字段
SELECT
    't_user.role字段' as "检查项",
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 't_user' AND column_name = 'role')
        THEN '通过'
        ELSE '失败'
    END as "状态";

-- 验证向量维度
SELECT
    't_vector_record向量维度' as "检查项",
    CASE
        WHEN EXISTS (SELECT 1 FROM t_vector_record WHERE embedding_dim != 1024)
        THEN '存在非1024维度数据'
        ELSE '所有维度一致(1024)'
    END as "状态";

-- 统计信息
\echo '\n--- 数据统计 ---'

SELECT 't_user' as "表名", COUNT(*) as "记录数" FROM t_user
UNION ALL
SELECT 't_document', COUNT(*) FROM t_document
UNION ALL
SELECT 't_document_chunk', COUNT(*) FROM t_document_chunk
UNION ALL
SELECT 't_vector_record', COUNT(*) FROM t_vector_record
UNION ALL
SELECT 't_qa_history', COUNT(*) FROM t_qa_history
UNION ALL
SELECT 't_citation', COUNT(*) FROM t_citation
ORDER BY 1;

\echo '\n修复脚本执行完成！'
