# RAG QA System 数据库问题排查与修复指南

## 常见问题

### 1. 缺少role字段错误

**错误信息:**
```
ERROR: column "role" of relation "t_user" does not exist
```

**原因:**
旧版本的数据库脚本没有包含role字段，但新版实体类User.java需要该字段。

**解决方案:**

方法1: 使用Flyway迁移（推荐）
```bash
mvn flyway:migrate
```

方法2: 手动添加字段
```sql
-- 添加role字段
ALTER TABLE t_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'STUDENT';

-- 添加约束
ALTER TABLE t_user ADD CONSTRAINT chk_user_role
    CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));

-- 添加索引
CREATE INDEX idx_user_role ON t_user(role);
```

### 2. 向量维度不匹配错误

**错误信息:**
```
ERROR: expected 1024 dimensions, not 4096
```

**原因:**
旧版本的init.sql使用4096维向量，但新版本统一使用1024维。

**解决方案:**

方法1: 如果数据库中还没有向量数据
```sql
-- 删除旧表重新创建
DROP TABLE IF EXISTS t_vector_record;
-- 然后重新运行schema.sql
```

方法2: 如果已有向量数据，需要重新生成
```sql
-- 删除所有向量记录
TRUNCATE TABLE t_vector_record;

-- 同时重置文档处理状态
UPDATE t_document SET status = 'UPLOADING', chunk_count = 0;
```

然后重新处理所有文档。

### 3. pgvector扩展未安装

**错误信息:**
```
ERROR: type "vector" does not exist
```

**原因:**
PostgreSQL未安装pgvector扩展。

**解决方案:**

```bash
# 使用psql安装扩展
psql -U postgres -d rag_qa_system -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

或者手动安装pgvector:
```bash
# 在PostgreSQL服务器上执行
git clone --branch v0.5.1 https://github.com/pgvector/pgvector.git
cd pgvector
make
make install
```

### 4. Flyway迁移失败

**错误信息:**
```
ERROR: Validate failed: migration checksum mismatch
```

**原因:**
已执行的迁移脚本被修改。

**解决方案:**

方法1: 修复schema历史表（如果确定修改是正确的）
```sql
-- 删除指定版本的校验和记录
DELETE FROM flyway_schema_history WHERE version = '1';

-- 重新运行迁移
```

方法2: 重置Flyway（仅开发环境）
```bash
# 清除Flyway历史（会删除所有数据！）
mvn flyway:clean

# 重新迁移
mvn flyway:migrate
```

**注意**: 生产环境不要执行clean操作！

### 5. 连接池耗尽

**错误信息:**
```
ERROR: connection pool is exhausted
```

**原因:**
数据库连接池配置不足，或连接未正确释放。

**解决方案:**

调整连接池配置（application.yml）:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30      # 增加最大连接数
      minimum-idle: 10           # 最小空闲连接
      connection-timeout: 30000  # 连接超时时间
      idle-timeout: 600000       # 空闲超时时间
      max-lifetime: 1800000      # 最大生命周期
```

同时检查代码中是否正确关闭资源:
```java
// 使用try-with-resources确保资源关闭
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // 执行查询
} // 自动关闭
```

## 数据库升级指南

### 从旧版本升级到新版本

1. **备份数据**
```bash
pg_dump -U postgres -d rag_qa_system > backup_$(date +%Y%m%d).sql
```

2. **应用Flyway迁移**
```bash
mvn flyway:migrate
```

3. **验证升级**
```bash
psql -U postgres -d rag_qa_system -f src/main/resources/db/verify_schema.sql
```

4. **检查应用日志**
启动应用并检查是否有数据库相关错误。

### 完全重新初始化（会丢失所有数据）

**警告: 此操作会删除所有数据！**

```bash
# 1. 停止应用

# 2. 清理Flyway
mvn flyway:clean

# 3. 重新迁移
mvn flyway:migrate

# 4. 启动应用
mvn spring-boot:run
```

## 获取帮助

如果遇到本文档未覆盖的问题:

1. 检查应用日志中的完整错误信息
2. 运行验证脚本检查数据库状态
3. 查看Flyway历史表了解迁移状态:
   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank;
   ```
