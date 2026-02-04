# RAG QA System 数据库文档

## 概述

本文档描述了RAG教学知识库问答系统的数据库结构和维护方法。

## 数据库版本

- **数据库**: PostgreSQL 15+
- **扩展**: pgvector (向量存储), uuid-ossp (UUID生成)
- **迁移工具**: Flyway 10.x

## 表结构

### 1. t_user (用户表)
存储系统用户信息，对应实体类: `User.java`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PK | 主键 |
| username | VARCHAR(100) | NOT NULL, UNIQUE | 用户名 |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| password | VARCHAR(255) | | 密码(BCrypt加密) |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'STUDENT' | 角色(ADMIN/TEACHER/STUDENT) |
| is_active | BOOLEAN | DEFAULT TRUE | 是否激活 |
| created_at | TIMESTAMP | | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |

### 2. t_document (文档表)
存储上传的文档元数据，对应实体类: `Document.java`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PK | 主键 |
| user_id | UUID | FK → t_user | 所属用户 |
| filename | VARCHAR(255) | NOT NULL | 文件名 |
| file_path | VARCHAR(500) | | 文件存储路径 |
| file_size | BIGINT | | 文件大小(字节) |
| file_type | VARCHAR(50) | | 文件类型(PDF/DOCX/PPTX/TXT) |
| status | VARCHAR(50) | DEFAULT 'UPLOADING' | 状态 |
| error_message | TEXT | | 错误信息 |
| uploaded_at | TIMESTAMP | | 上传时间 |
| processed_at | TIMESTAMP | | 处理完成时间 |
| chunk_count | INT | DEFAULT 0 | 分块数量 |
| description | VARCHAR(500) | | 描述 |
| created_at | TIMESTAMP | | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |

### 3. t_document_chunk (文档分块表)
存储文档切分后的文本块，对应实体类: `DocumentChunk.java`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PK | 主键 |
| document_id | UUID | FK → t_document | 所属文档 |
| chunk_index | INT | NOT NULL | 分块索引 |
| content | TEXT | NOT NULL | 文本内容 |
| content_length | INT | | 内容长度 |
| page_num | INT | | 页码 |
| section_title | VARCHAR(255) | | 章节标题 |
| char_start | INT | | 起始字符位置 |
| char_end | INT | | 结束字符位置 |
| metadata | JSONB | | 元数据 |
| created_at | TIMESTAMP | | 创建时间 |

### 4. t_vector_record (向量记录表)
存储文档分块的向量表示，对应实体类: `VectorRecord.java`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PK | 主键 |
| chunk_id | UUID | FK → t_document_chunk | 所属分块 |
| document_id | UUID | FK → t_document | 所属文档 |
| embedding | vector(1024) | | 向量数据(1024维) |
| embedding_dim | INT | DEFAULT 1024 | 向量维度 |
| embedding_model | VARCHAR(100) | | 嵌入模型 |
| created_at | TIMESTAMP | | 创建时间 |

### 5. t_qa_history (问答历史表)
记录用户的提问和系统回答，对应实体类: `QaHistory.java`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PK | 主键 |
| user_id | UUID | FK → t_user | 所属用户 |
| question | VARCHAR(1000) | NOT NULL | 问题 |
| answer | TEXT | NOT NULL | 答案 |
| response_time | INT | | 响应时间(毫秒) |
| retrieved_chunks | TEXT | | 检索到的分块ID |
| retrieved_documents | TEXT | | 检索到的文档ID |
| model_version | VARCHAR(100) | | 模型版本 |
| asked_at | TIMESTAMP | | 提问时间 |
| created_at | TIMESTAMP | | 创建时间 |

### 6. t_citation (引用来源表)
记录答案引用的文档来源，对应实体类: `Citation.java`

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PK | 主键 |
| qa_id | UUID | FK → t_qa_history | 所属问答记录 |
| chunk_id | UUID | FK → t_document_chunk | 引用分块 |
| document_id | UUID | FK → t_document | 引用文档 |
| page_num | INT | | 页码 |
| chunk_index | INT | | 分块索引 |
| relevance_score | FLOAT | | 相关性分数(0-1) |
| citation_text | VARCHAR(500) | | 引用文本 |
| created_at | TIMESTAMP | | 创建时间 |

## 数据库迁移

### 使用Flyway迁移

项目使用Flyway进行数据库版本管理。

#### 迁移脚本位置
```
src/main/resources/db/migration/
├── V1__Initial_schema.sql        # 初始表结构
├── V2__Add_indexes_and_functions.sql  # 索引和函数
├── V3__Add_views_and_seed_data.sql    # 视图和初始数据
└── V4__Fix_existing_data.sql     # 数据修复（升级时使用）
```

#### 执行迁移
```bash
# 使用Maven
mvn flyway:migrate

# 或使用Spring Boot启动
mvn spring-boot:run
```

### 手动执行脚本

如果不使用Flyway，可以手动执行SQL脚本：

```bash
# 连接PostgreSQL并执行脚本
psql -U postgres -d rag_qa_system -f src/main/resources/db/schema.sql
```

## 验证脚本

使用验证脚本检查数据库结构：

```bash
psql -U postgres -d rag_qa_system -f src/main/resources/db/verify_schema.sql
```

## 注意事项

1. **向量维度**: 项目使用1024维向量，确保 `t_vector_record.embedding` 字段维度正确
2. **角色字段**: `t_user.role` 字段必须存在，支持 STUDENT/TEACHER/ADMIN 三种角色
3. **pgvector扩展**: 必须安装pgvector扩展才能使用向量功能
4. **初始账号**: 默认创建 student/teacher/admin 三个账号，密码均为 123456
