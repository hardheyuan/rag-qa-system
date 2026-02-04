# RAG项目安全问题修复计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修复项目中的硬编码凭证、数据库初始化问题以及代码质量问题

**Architecture:** 使用环境变量管理敏感配置，Flyway管理数据库迁移，统一异常处理和代码重构

**Tech Stack:** Spring Boot 3.2, PostgreSQL, Flyway, Docker

---

## 任务概览

### 高优先级（安全风险）
- **Task 1:** 移除硬编码API密钥和JWT密钥
- **Task 2:** 数据库初始化脚本检查与修复
- **Task 3:** 修复application.yml硬编码密码

### 中优先级（代码质量）
- **Task 4:** 提取重复代码到工具类
- **Task 5:** 完善异常处理机制
- **Task 6:** 添加基础单元测试

### 低优先级（性能优化）
- **Task 7:** 优化向量检索性能
- **Task 8:** 文档处理异步队列优化

---

## Task 1: 移除硬编码API密钥和JWT密钥

**目标：** 将application.yml中的硬编码敏感信息改为环境变量

**Files:**
- Read: `demo1/src/main/resources/application.yml`
- Create: `demo1/.env.example`
- Create: `demo1/src/main/resources/application-local.yml`
- Modify: `demo1/src/main/resources/application.yml`

**步骤详情：**

### Step 1: 查看当前application.yml中的硬编码配置

运行以下命令查看当前的application.yml内容，找出所有硬编码的敏感信息：

```bash
cat demo1/src/main/resources/application.yml | head -100
```

需要找出：
- ModelScope API密钥
- 阿里云OCR AppCode
- JWT密钥
- 数据库密码

### Step 2: 创建环境变量模板文件

Create: `demo1/.env.example`

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=rag_qa_system
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# ModelScope API密钥（用于LLM调用）
MODELSCOPE_API_KEY=your_modelscope_api_key

# 阿里云OCR配置（文档文字识别）
ALIYUN_OCR_APPCODE=your_aliyun_ocr_appcode

# JWT配置（Token签名密钥，至少256位）
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_at_least_64_characters
JWT_ACCESS_TOKEN_EXPIRATION=1800000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# CORS配置（前端地址）
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# 文件上传配置
UPLOAD_MAX_FILE_SIZE=50MB
UPLOAD_MAX_REQUEST_SIZE=100MB
```

### Step 3: 创建本地开发配置文件

Create: `demo1/src/main/resources/application-local.yml`

```yaml
# 本地开发环境配置 - 从环境变量读取敏感信息
spring:
  config:
    import: optional:file:.env[.properties]
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:rag_qa_system}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

# LangChain4j配置
langchain4j:
  modelscope:
    api-key: ${MODELSCOPE_API_KEY:}
    model-name: deepseek-ai/DeepSeek-R1-0528
    base-url: https://api.modelscope.cn/v1
    temperature: 0.7
    max-tokens: 2048
  embedding:
    model-name: iic/gte-Qwen2-7B-instruct

# JWT配置
jwt:
  secret: ${JWT_SECRET:}
  access-token:
    expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:1800000}
  refresh-token:
    expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:604800000}

# 阿里云OCR配置
aliyun:
  ocr:
    appcode: ${ALIYUN_OCR_APPCODE:}
    host: https://ocrdoc.market.alicloudapi.com

# CORS配置
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}

# 日志配置
logging:
  level:
    com.hiyuan.demo1: DEBUG
    org.springframework.security: DEBUG
  file:
    name: logs/application-local.log
```

### Step 4: 修改主application.yml移除硬编码

Edit: `demo1/src/main/resources/application.yml`

移除所有硬编码的敏感信息，改为从环境变量读取。

### Step 5: 添加dotenv依赖到pom.xml

Edit: `demo1/pom.xml`

添加：
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Step 6: 创建环境变量加载配置类

Create: `demo1/src/main/java/com/hiyuan/demo1/config/EnvConfig.java`

### Step 7: 更新.gitignore

Edit: `demo1/.gitignore`

添加：
```gitignore
# 环境变量文件（包含敏感信息）
.env
.env.local
.env.*.local
```

### Step 8: 验证配置

运行应用测试环境变量是否正确加载：

```bash
# 1. 复制环境变量模板
cp .env.example .env

# 2. 编辑.env文件，填入实际的敏感信息
notepad .env

# 3. 使用local profile启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 执行计划确认

**计划完成并保存到 `docs/plans/2026-02-03-rag-security-issues-fix.md`。两种执行选项：**

**1. Subagent-Driven (this session)** - 我为每个任务调度新的子代理，在任务之间进行审查，快速迭代

**2. Parallel Session (separate)** - 打开新会话并使用 executing-plans 技能，批量执行并设置检查点

**你选择的是：选项 1 - Subagent-Driven (当前会话)**

---

## 接下来执行 Task 1

我现在将调度第一个子代理来执行 **Task 1: 移除硬编码API密钥和JWT密钥**。

**子代理将执行：**
1. 查看当前application.yml中的硬编码配置
2. 创建环境变量模板文件 (.env.example)
3. 创建本地开发配置文件 (application-local.yml)
4. 修改主application.yml移除硬编码
5. 添加dotenv依赖到pom.xml
6. 创建环境变量加载配置类 (EnvConfig.java)
7. 更新.gitignore
8. 验证配置

**准备好了吗？我现在开始调度子代理执行任务 1。**