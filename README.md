# RAG QA System - 教学知识库智能问答系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-green?style=flat-square&logo=spring" alt="Spring Boot 3.2">
  <img src="https://img.shields.io/badge/Java-21-blue?style=flat-square&logo=java" alt="Java 21">
  <img src="https://img.shields.io/badge/Vue-3.5.24-4FC08D?style=flat-square&logo=vue.js" alt="Vue 3">
  <img src="https://img.shields.io/badge/Tailwind-4.1.18-38B2AC?style=flat-square&logo=tailwind-css" alt="Tailwind CSS">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License">
</p>

## 项目简介

RAG QA System 是一个基于检索增强生成（Retrieval-Augmented Generation）的智能问答系统，专为教学知识库设计。系统支持用户上传各类文档（PDF、Word、PPT、TXT等），自动解析并建立向量索引，通过AI大模型提供精准的智能问答服务。

### 核心特性

- **文档智能处理**：支持PDF、DOCX、PPTX、TXT等多种格式，自动解析并生成向量索引
- **RAG智能问答**：基于检索增强生成技术，结合上下文提供精准答案
- **流式响应**：支持SSE流式输出，实时展示AI生成内容
- **向量化存储**：采用PostgreSQL + pgvector，支持MRL（Matryoshka Representation Learning）
- **JWT认证**：基于Access Token + Refresh Token的双令牌认证机制
- **异步处理**：文档解析和向量生成采用异步处理，提升系统响应速度

## 技术栈

### 后端 (Backend)

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Java | 21 | 编程语言 |
| Maven | 3.9+ | 构建工具 |
| PostgreSQL | 15 | 关系型数据库 |
| pgvector | 0.5.1 | 向量存储扩展 |
| LangChain4j | 0.35.0 | AI/LLM框架 |
| DeepSeek-R1 | - | 大语言模型（通过ModelScope） |
| Qwen3-Embedding | - | 向量嵌入模型 |
| JWT (jjwt) | 0.12.3 | 认证令牌 |
| Flyway | 10.6.0 | 数据库迁移 |
| PDFBox | 3.0.3 | PDF解析 |
| Apache POI | 5.3.0 | Office文档解析 |

### 前端 (Frontend)

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.24 | 前端框架 |
| Vite | 7.2.4 | 构建工具 |
| Vue Router | 5.0.1 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Tailwind CSS | 4.1.18 | CSS框架 |
| VueUse | 14.2.0 | 工具库 |

## 项目结构

```
myProject/
├── demo1/                          # Spring Boot 后端
│   ├── src/main/java/com/hiyuan/demo1/
│   │   ├── config/                 # 配置类
│   │   ├── controller/             # REST API 控制器
│   │   ├── service/                # 业务逻辑
│   │   ├── repository/             # 数据访问层
│   │   ├── entity/                 # JPA 实体
│   │   ├── dto/                    # 数据传输对象
│   │   ├── security/               # JWT 认证
│   │   └── exception/              # 自定义异常
│   ├── src/main/resources/
│   │   ├── application.yml         # 主配置
│   │   └── db/migration/           # Flyway 迁移脚本
│   └── pom.xml                     # Maven 配置
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── components/             # Vue 组件
│   │   ├── views/                  # 页面组件
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── api/                    # API 客户端
│   │   └── router/                 # 路由配置
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml              # Docker 服务配置
└── README.md                       # 项目文档
```

## 快速开始

### 环境要求

- **Java**: JDK 21 或更高版本
- **Maven**: 3.9 或更高版本
- **Node.js**: 18.x 或更高版本
- **Docker**: 20.x 或更高版本（用于 PostgreSQL）
- **Git**: 2.x 或更高版本

### 1. 克隆项目

```bash
git clone <repository-url>
cd myProject
```

### 2. 配置环境变量

在后端目录创建 `.env` 文件，配置以下环境变量：

```bash
cd demo1

# 创建 .env 文件
touch .env
```

编辑 `.env` 文件，添加以下内容：

```env
# ModelScope API Key (必须)
MODELSCOPE_API_KEY=your_modelscope_api_key_here

# JWT Secret (必须，至少32个字符)
JWT_SECRET=your_jwt_secret_key_here_at_least_32_characters_long

# 数据库密码
DB_PASSWORD=your_database_password

# 阿里云 OCR (可选，用于扫描PDF识别)
ALIYUN_OCR_APPCODE=your_aliyun_ocr_appcode
```

### 3. 启动 PostgreSQL 数据库

在项目根目录执行：

```bash
cd ..  # 回到项目根目录
docker-compose up -d postgres
```

等待数据库启动完成（约30秒）：

```bash
# 检查数据库状态
docker-compose ps

# 查看日志
docker-compose logs -f postgres
```

### 4. 启动后端服务

```bash
cd demo1

# 方式1: 使用 Maven 运行
mvn spring-boot:run

# 方式2: 先打包再运行
mvn clean package
java -jar target/rag-qa-system-1.0.0-SNAPSHOT.jar
```

后端服务将在 `http://localhost:8080` 启动。

验证服务是否正常运行：

```bash
curl http://localhost:8080/api/system/health
```

### 5. 启动前端服务

打开新的终端窗口：

```bash
cd myProject/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5173` 启动。

访问 `http://localhost:5173` 即可使用系统。

## 开发指南

### 后端开发

#### 常用命令

```bash
cd demo1

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=AuthControllerTest

# 使用开发环境配置运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生成代码覆盖率报告
mvn jacoco:report
```

#### 数据库迁移

使用 Flyway 管理数据库迁移：

```bash
# 迁移脚本位置: demo1/src/main/resources/db/migration/

# 命名规范: V{version}__{description}.sql
# 示例: V1__init_schema.sql, V2__add_user_table.sql
```

### 前端开发

#### 常用命令

```bash
cd frontend

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 运行测试
npm run test

# 持续运行测试
npm run test:watch
```

#### 项目结构

```
frontend/src/
├── components/          # 可复用组件
│   ├── common/          # 通用组件
│   ├── documents/       # 文档相关组件
│   └── qa/              # 问答相关组件
├── views/               # 页面组件
│   ├── Home.vue
│   ├── Login.vue
│   ├── Register.vue
│   ├── Documents.vue
│   ├── QaChat.vue
│   └── History.vue
├── stores/              # Pinia 状态管理
│   ├── auth.js
│   ├── documents.js
│   └── qa.js
├── api/                 # API 客户端
│   ├── client.js
│   ├── auth.js
│   ├── documents.js
│   └── qa.js
├── router/              # 路由配置
│   └── index.js
├── composables/         # 组合式函数
│   └── useAuth.js
└── utils/               # 工具函数
    └── format.js
```

## 部署指南

### Docker 部署

#### 1. 构建后端镜像

```bash
cd demo1

# 构建镜像
docker build -t rag-qa-backend:latest .

# 或者使用 Maven 插件
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=rag-qa-backend:latest
```

#### 2. 构建前端镜像

```bash
cd frontend

# 构建生产版本
npm run build

# 构建 Nginx 镜像
docker build -t rag-qa-frontend:latest .
```

#### 3. 使用 Docker Compose 部署

```bash
# 在项目根目录执行
docker-compose -f docker-compose.prod.yml up -d
```

### 生产环境配置

#### 后端配置 (application-prod.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/rag_qa_system
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

langchain4j:
  modelscope:
    api-key: ${MODELSCOPE_API_KEY}
    base-url: https://api-inference.modelscope.cn/v1
    chat-model: deepseek-ai/DeepSeek-R1-0528
    embedding-model: Qwen/Qwen3-Embedding-0.6B

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 1800000  # 30分钟
  refresh-token-expiration: 604800000  # 7天
```

#### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # SSE 支持
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 3600s;
    }
}
```

## 环境变量说明

| 变量名 | 必填 | 说明 |
|--------|------|------|
| `MODELSCOPE_API_KEY` | 是 | ModelScope API 密钥，用于访问DeepSeek和Embedding模型 |
| `JWT_SECRET` | 是 | JWT签名密钥，至少32个字符 |
| `DB_PASSWORD` | 是 | PostgreSQL数据库密码 |
| `DB_USERNAME` | 否 | 数据库用户名，默认postgres |
| `ALIYUN_OCR_APPCODE` | 否 | 阿里云OCR服务AppCode，用于扫描PDF识别 |

## API 文档

完整的 API 文档请查看 [参考文档/API-DOCUMENTATION.md](./参考文档/API-DOCUMENTATION.md)。

### 主要接口概览

| 模块 | 接口 | 方法 | 路径 |
|------|------|------|------|
| 系统 | 健康检查 | GET | /api/system/health |
| 认证 | 用户登录 | POST | /api/auth/login |
| 认证 | 用户注册 | POST | /api/auth/register |
| 认证 | 刷新令牌 | POST | /api/auth/refresh |
| 文档 | 上传文档 | POST | /api/documents/upload |
| 文档 | 文档列表 | GET | /api/documents |
| 问答 | 智能问答 | POST | /api/qa/ask |
| 历史 | 历史记录 | GET | /api/history |

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Vue 3 前端    │────▶│  Spring Boot    │────▶│  PostgreSQL     │
│  (Vite +        │◀────│   后端服务       │◀────│  + pgvector     │
│  Tailwind CSS)  │     │                 │     │                 │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                               │
                               ▼
                    ┌─────────────────┐
                    │  ModelScope API │
                    │  - DeepSeek-R1  │
                    │  - Qwen3-Embed  │
                    └─────────────────┘
```

## 开发计划

- [x] 项目基础架构搭建
- [x] 数据库设计与实现
- [x] JWT 认证系统
- [x] 文档上传与解析
- [x] 向量嵌入与存储
- [x] RAG 问答系统
- [x] 前端界面开发
- [x] SSE 流式输出
- [ ] 文档OCR识别增强
- [ ] 对话历史管理
- [ ] 系统监控与统计

## 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -am 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

## 许可证

本项目基于 [MIT 许可证](LICENSE) 开源。

## 联系方式

- 项目主页：[GitHub Repository](https://github.com/your-username/rag-qa-system)
- 问题反馈：[GitHub Issues](https://github.com/your-username/rag-qa-system/issues)
- 邮箱：your-email@example.com

---

<p align="center">Made with ❤️ by Your Team</p>
