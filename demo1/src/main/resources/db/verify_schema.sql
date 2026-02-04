-- ============================================
-- RAG 教学知识库问答系统 - 数据库 Schema 验证脚本
-- 用途: 验证数据库表结构是否与实体类定义一致
-- ============================================

-- 设置输出格式
\pset pager off
\pset format aligned
\pset border 2
\pset tuples_only off

-- ============================================
-- 1. 验证扩展
-- ============================================
\echo '\n==========================================='
\echo '1. 验证 PostgreSQL 扩展'
\echo '==========================================='

SELECT
    extname as "扩展名称",
    extversion as "版本",
    CASE WHEN extname IN ('vector', 'uuid-ossp') THEN '必需 - 已安装' ELSE '可选' END as "状态"
FROM pg_extension
WHERE extname IN ('vector', 'uuid-ossp')
ORDER BY extname;

-- ============================================
-- 2. 验证表结构
-- ============================================
\echo '\n==========================================='
\echo '2. 验证表结构'
\echo '==========================================='

SELECT
    t.table_name as "表名",
    COUNT(c.column_name) as "字段数",
    CASE
        WHEN t.table_name = 't_user' THEN '用户表 - User实体'
        WHEN t.table_name = 't_document' THEN '文档表 - Document实体'
        WHEN t.table_name = 't_document_chunk' THEN '文档分块表 - DocumentChunk实体'
        WHEN t.table_name = 't_vector_record' THEN '向量记录表 - VectorRecord实体'
        WHEN t.table_name = 't_qa_history' THEN '问答历史表 - QaHistory实体'
        WHEN t.table_name = 't_citation' THEN '引用来源表 - Citation实体'
        ELSE '未知表'
    END as "说明"
FROM information_schema.tables t
LEFT JOIN information_schema.columns c ON t.table_name = c.table_name
WHERE t.table_schema = 'public'
  AND t.table_type = 'BASE TABLE'
  AND t.table_name LIKE 't_%'
GROUP BY t.table_name
ORDER BY t.table_name;

-- 检查是否缺少表
\echo '\n--- 表完整性检查 ---'

DO $$
DECLARE
    required_tables TEXT[] := ARRAY['t_user', 't_document', 't_document_chunk', 't_vector_record', 't_qa_history', 't_citation'];
    missing_tables TEXT[] := ARRAY[]::TEXT[];
    tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY required_tables
    LOOP
        IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = tbl) THEN
            missing_tables := array_append(missing_tables, tbl);
        END IF;
    END LOOP;

    IF array_length(missing_tables, 1) IS NULL THEN
        RAISE NOTICE '✓ 所有必需表都存在';
    ELSE
        RAISE NOTICE '✗ 缺少以下表: %', array_to_string(missing_tables, ', ');
    END IF;
END $$;

-- ============================================
-- 3. 验证关键字段
-- ============================================
\echo '\n==========================================='
\echo '3. 验证关键字段'
\echo '==========================================='

-- 检查t_user表的role字段（新增字段）
\echo '\n--- t_user 表字段检查 ---'

SELECT
    column_name as "字段名",
    data_type as "数据类型",
    is_nullable as "可空",
    column_default as "默认值"
FROM information_schema.columns
WHERE table_name = 't_user'
ORDER BY ordinal_position;

-- ============================================
-- 4. 验证索引
-- ============================================
\echo '\n==========================================='
\echo '4. 验证索引'
\echo '==========================================='

SELECT
    tablename as "表名",
    indexname as "索引名",
    CASE
        WHEN indexdef LIKE '%UNIQUE%' THEN '唯一索引'
        WHEN indexdef LIKE '%USING hnsw%' THEN 'HNSW向量索引'
        WHEN indexdef LIKE '%USING gin%' THEN 'GIN索引'
        ELSE '普通索引'
    END as "类型"
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename LIKE 't_%'
ORDER BY tablename, indexname;

-- ============================================
-- 5. 验证外键约束
-- ============================================
\echo '\n==========================================='
\echo '5. 验证外键约束'
\echo '==========================================='

SELECT
    tc.table_name as "表名",
    kcu.column_name as "字段名",
    ccu.table_name as "引用表",
    ccu.column_name as "引用字段"
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

-- ============================================
-- 6. 验证视图
-- ============================================
\echo '\n==========================================='
\echo '6. 验证视图'
\echo '==========================================='

SELECT
    table_name as "视图名",
    CASE
        WHEN table_name = 'v_document_stats' THEN '文档统计视图 - 显示文档的详细统计信息'
        WHEN table_name = 'v_user_qa_stats' THEN '用户问答统计视图 - 显示用户的问答活动统计'
        ELSE '自定义视图'
    END as "说明"
FROM information_schema.views
WHERE table_schema = 'public'
ORDER BY table_name;

-- ============================================
-- 7. 验证函数
-- ============================================
\echo '\n==========================================='
\echo '7. 验证函数'
\echo '==========================================='

SELECT
    routine_name as "函数名",
    routine_type as "类型",
    CASE
        WHEN routine_name = 'search_similar_vectors' THEN '向量相似度搜索函数'
        WHEN routine_name = 'cleanup_orphaned_data' THEN '清理孤立数据函数'
        WHEN routine_name = 'update_updated_at_column' THEN '自动更新updated_at触发器函数'
        ELSE '自定义函数'
    END as "说明"
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_type = 'FUNCTION'
ORDER BY routine_name;

-- ============================================
-- 8. 数据验证
-- ============================================
\echo '\n==========================================='
\echo '8. 初始数据验证'
\echo '==========================================='

SELECT
    username as "用户名",
    email as "邮箱",
    role as "角色",
    is_active as "激活状态",
    created_at as "创建时间"
FROM t_user
ORDER BY role, username;

-- ============================================
-- 验证完成
-- ============================================
\echo '\n==========================================='
\echo '数据库 Schema 验证完成！'
\echo '==========================================='
