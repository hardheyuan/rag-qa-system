# Docker PostgreSQL 部署指南

## 📋 前置要求

- Docker Desktop 已安装并运行
- 端口 5432 未被占用

## 🚀 快速启动

### 1. 启动 PostgreSQL 容器

在项目根目录执行：

```bash
docker-compose up -d
```

### 2. 查看容器状态

```bash
docker-compose ps
```

应该看到：
```
NAME            IMAGE                    STATUS         PORTS
rag-postgres    pgvector/pgvector:pg15   Up (healthy)   0.0.0.0:5432->5432/tcp
```

### 3. 查看初始化日志

```bash
docker-compose logs postgres
```

应该看到 "数据库初始化完成" 的消息。

### 4. 验证数据库

连接到数据库：

```bash
docker exec -it rag-postgres psql -U postgres -d rag_qa_system
```

执行验证查询：

```sql
-- 查看所有表
\dt

-- 查看扩展
SELECT extname FROM pg_extension;

-- 查看用户
SELECT username, email FROM t_user;

-- 退出
\q
```

## 🔧 常用命令

### 停止容器

```bash
docker-compose stop
```

### 启动容器

```bash
docker-compose start
```

### 重启容器

```bash
docker-compose restart
```

### 停止并删除容器（保留数据）

```bash
docker-compose down
```

### 停止并删除容器和数据（危险！）

```bash
docker-compose down -v
```

### 查看日志

```bash
# 实时查看日志
docker-compose logs -f postgres

# 查看最近100行日志
docker-compose logs --tail=100 postgres
```

## 🗄️ 数据库连接信息

| 参数 | 值 |
|------|-----|
| 主机 | localhost |
| 端口 | 5432 |
| 数据库名 | rag_qa_system |
| 用户名 | postgres |
| 密码 | Qwe2003413. |

### JDBC 连接字符串

```
jdbc:postgresql://localhost:5432/rag_qa_system
```

### Spring Boot 配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rag_qa_system
    username: postgres
    password: Qwe2003413.
    driver-class-name: org.postgresql.Driver
```

## 📊 数据库结构

### 核心表

1. **t_user** - 用户表
2. **t_document** - 文档表
3. **t_document_chunk** - 文档分块表
4. **t_vector_record** - 向量记录表（核心）
5. **t_qa_history** - 问答历史表
6. **t_citation** - 引用来源表

### 视图

1. **v_document_stats** - 文档统计视图
2. **v_user_qa_stats** - 用户问答统计视图

### 函数

1. **search_similar_vectors()** - 向量相似度搜索
2. **cleanup_orphaned_data()** - 清理孤立数据

## 🔍 常用查询

### 查看文档统计

```sql
SELECT * FROM v_document_stats;
```

### 查看用户问答统计

```sql
SELECT * FROM v_user_qa_stats;
```

### 向量相似度搜索示例

```sql
-- 需要先有向量数据
SELECT * FROM search_similar_vectors(
    '[0.1, 0.2, ...]'::vector(4096),  -- 查询向量
    '00000000-0000-0000-0000-000000000001'::uuid,  -- 用户ID
    5  -- 返回前5个结果
);
```

### 清理孤立数据

```sql
SELECT * FROM cleanup_orphaned_data();
```

## 🛠️ 故障排查

### 问题1: 端口被占用

**错误信息：**
```
Error: bind: address already in use
```

**解决方案：**

1. 检查是否有其他 PostgreSQL 在运行：
```bash
# Windows
netstat -ano | findstr :5432

# 停止占用端口的进程
taskkill /PID <进程ID> /F
```

2. 或者修改 docker-compose.yml 中的端口映射：
```yaml
ports:
  - "5433:5432"  # 改用5433端口
```

### 问题2: 容器启动失败

**查看详细日志：**
```bash
docker-compose logs postgres
```

**重新构建：**
```bash
docker-compose down -v
docker-compose up -d
```

### 问题3: 初始化脚本未执行

**原因：** 数据卷已存在，初始化脚本只在首次创建时执行。

**解决方案：**
```bash
# 删除数据卷重新初始化
docker-compose down -v
docker-compose up -d
```

### 问题4: 无法连接数据库

**检查容器状态：**
```bash
docker-compose ps
```

**检查健康状态：**
```bash
docker inspect rag-postgres | grep -A 10 Health
```

**测试连接：**
```bash
docker exec -it rag-postgres pg_isready -U postgres
```

## 📦 备份和恢复

### 备份数据库

```bash
# 备份到文件
docker exec -t rag-postgres pg_dump -U postgres rag_qa_system > backup.sql

# 备份到压缩文件
docker exec -t rag-postgres pg_dump -U postgres rag_qa_system | gzip > backup.sql.gz
```

### 恢复数据库

```bash
# 从备份文件恢复
docker exec -i rag-postgres psql -U postgres rag_qa_system < backup.sql

# 从压缩文件恢复
gunzip -c backup.sql.gz | docker exec -i rag-postgres psql -U postgres rag_qa_system
```

### 导出特定表

```bash
docker exec -t rag-postgres pg_dump -U postgres -t t_document rag_qa_system > documents.sql
```

## 🔐 安全建议

### 生产环境配置

1. **修改默认密码**

编辑 docker-compose.yml：
```yaml
environment:
  POSTGRES_PASSWORD: your_strong_password_here
```

2. **限制网络访问**

```yaml
ports:
  - "127.0.0.1:5432:5432"  # 只允许本地访问
```

3. **使用环境变量**

创建 .env 文件：
```env
POSTGRES_PASSWORD=your_strong_password
```

修改 docker-compose.yml：
```yaml
environment:
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
```

4. **定期备份**

设置定时任务自动备份数据库。

## 📈 性能优化

### 调整 PostgreSQL 配置

创建 `postgresql.conf` 文件：

```conf
# 内存设置
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB
maintenance_work_mem = 128MB

# 连接设置
max_connections = 100

# 查询优化
random_page_cost = 1.1
effective_io_concurrency = 200
```

修改 docker-compose.yml：
```yaml
volumes:
  - ./postgresql.conf:/etc/postgresql/postgresql.conf
command: postgres -c config_file=/etc/postgresql/postgresql.conf
```

### 监控数据库性能

```sql
-- 查看活动连接
SELECT * FROM pg_stat_activity;

-- 查看表大小
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 查看索引使用情况
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

## 🎯 下一步

1. 启动 Spring Boot 应用
2. 测试文档上传功能
3. 测试问答功能
4. 查看数据库中的数据变化

## 📞 支持

如有问题，请查看：
- Docker 日志: `docker-compose logs postgres`
- PostgreSQL 日志: 在容器内查看 `/var/log/postgresql/`
- 应用日志: `demo1/logs/application.log`
