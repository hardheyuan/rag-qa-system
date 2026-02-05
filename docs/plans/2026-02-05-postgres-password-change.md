# PostgreSQL 密码修改记录

**日期**: 2026-02-05  
**修改内容**: 将 PostgreSQL 数据库密码从 `Qwe2003413.` 改为 `123456`  
**状态**: ✅ 已完成

## 修改的文件

### 1. docker-compose.yml
Docker Compose 配置文件，用于本地开发环境。

**修改内容**:
```yaml
POSTGRES_PASSWORD: 123456  # 原: Qwe2003413.
```

### 2. demo1/src/main/resources/application.yml
Spring Boot 应用配置文件。

**修改内容**:
```yaml
password: ${DB_PASSWORD:123456}  # 原: ${DB_PASSWORD:Qwe2003413.}
```

说明：
- 优先使用环境变量 `DB_PASSWORD`
- 如果环境变量不存在，使用默认值 `123456`

### 3. .env.example
环境变量配置示例文件。

**修改内容**:
```env
POSTGRES_PASSWORD=123456  # 原: your_secure_database_password_here
```

## 如何应用修改

### 方式1：使用 Docker Compose（推荐）

如果使用 Docker 运行数据库：

```bash
# 1. 停止并删除现有容器和数据卷
docker-compose down -v

# 2. 重新启动（会使用新密码创建数据库）
docker-compose up -d postgres

# 3. 验证数据库连接
docker exec -it rag-postgres psql -U postgres -d rag_qa_system
```

**注意**: `-v` 参数会删除数据卷，所有数据将丢失！如果需要保留数据，请先备份。

### 方式2：修改现有 PostgreSQL 密码

如果已有数据不想丢失，可以直接修改 PostgreSQL 用户密码：

```bash
# 进入 PostgreSQL 容器
docker exec -it rag-postgres psql -U postgres

# 在 psql 中执行
ALTER USER postgres WITH PASSWORD '123456';
\q
```

### 方式3：使用环境变量（生产环境推荐）

创建 `.env` 文件（不要提交到 Git）：

```bash
# 复制示例文件
cp .env.example .env

# 编辑 .env 文件
# POSTGRES_PASSWORD=123456
```

然后使用 `docker-compose.prod.yml`：

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 验证修改

### 1. 测试数据库连接

使用 psql 命令行：
```bash
psql -h localhost -p 5432 -U postgres -d rag_qa_system
# 输入密码: 123456
```

使用 Docker：
```bash
docker exec -it rag-postgres psql -U postgres -d rag_qa_system
```

### 2. 测试 Spring Boot 应用

启动应用并查看日志：
```bash
cd demo1
mvn spring-boot:run
```

如果看到类似以下日志，说明连接成功：
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

### 3. 测试 API

```bash
curl http://localhost:8080/api/actuator/health
```

应该返回：
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

## 安全建议

⚠️ **重要提示**: `123456` 是一个非常弱的密码，仅适合本地开发环境使用。

### 生产环境密码要求

生产环境应使用强密码，建议：
- 至少 16 个字符
- 包含大小写字母、数字和特殊字符
- 使用密码生成器生成随机密码

生成强密码示例：
```bash
# Linux/Mac
openssl rand -base64 24

# 或使用 pwgen
pwgen -s 24 1
```

### 密码管理最佳实践

1. **不要在代码中硬编码密码**
   - 使用环境变量
   - 使用密钥管理服务（如 AWS Secrets Manager、Azure Key Vault）

2. **不要提交 .env 文件到 Git**
   - 已在 `.gitignore` 中排除
   - 只提交 `.env.example` 作为模板

3. **定期更换密码**
   - 生产环境建议每 90 天更换一次
   - 发生安全事件后立即更换

4. **限制数据库访问**
   - 只允许应用服务器 IP 访问
   - 使用防火墙规则
   - 不要暴露数据库端口到公网

## 相关配置文件

### 开发环境
- `docker-compose.yml` - 本地开发 Docker 配置
- `demo1/src/main/resources/application.yml` - Spring Boot 配置

### 生产环境
- `docker-compose.prod.yml` - 生产环境 Docker 配置（使用环境变量）
- `.env` - 环境变量配置（需手动创建，不提交到 Git）
- `.env.example` - 环境变量模板

## 故障排查

### 问题1: 连接被拒绝

```
Connection refused: connect
```

**解决方案**:
- 检查 PostgreSQL 容器是否运行: `docker ps`
- 检查端口是否正确: `5432`
- 检查防火墙设置

### 问题2: 密码认证失败

```
FATAL: password authentication failed for user "postgres"
```

**解决方案**:
- 确认密码已修改: `123456`
- 如果使用 Docker，重新创建容器: `docker-compose down -v && docker-compose up -d`
- 检查环境变量是否正确设置

### 问题3: 数据库不存在

```
FATAL: database "rag_qa_system" does not exist
```

**解决方案**:
- 重新创建数据库容器
- 或手动创建数据库:
  ```bash
  docker exec -it rag-postgres psql -U postgres
  CREATE DATABASE rag_qa_system;
  ```

## 总结

PostgreSQL 密码已成功修改为 `123456`。修改涉及 3 个配置文件，适用于本地开发环境。

**下一步**:
1. 重启 Docker 容器应用新密码
2. 测试应用连接
3. 如果是生产环境，请使用强密码并通过环境变量配置
