# RAG QA System - 前后端接口文档

**版本**: v1.0.0  
**更新日期**: 2026-01-09  
**基础URL**: `http://localhost:8080/api`

---

## 目录

1. [接口概览](#接口概览)
2. [通用规范](#通用规范)
3. [系统接口](#系统接口)
4. [文档管理接口](#文档管理接口)
5. [问答接口](#问答接口)
6. [历史记录接口](#历史记录接口)
7. [统计接口](#统计接口)
8. [错误码说明](#错误码说明)
9. [前端调用示例](#前端调用示例)

---

## 接口概览

### 接口列表

| 模块 | 接口名称 | 方法 | 路径 | 状态 |
|------|----------|------|------|------|
| **系统** | 健康检查 | GET | `/system/health` | ✅ 已实现 |
| **系统** | 系统状态 | GET | `/system/status` | ✅ 已实现 |
| **文档** | 上传文档 | POST | `/documents/upload` | 🔄 待实现 |
| **文档** | 文档列表 | GET | `/documents` | 🔄 待实现 |
| **文档** | 文档详情 | GET | `/documents/{id}` | 🔄 待实现 |
| **文档** | 删除文档 | DELETE | `/documents/{id}` | 🔄 待实现 |
| **问答** | 智能问答 | POST | `/qa/ask` | 🔄 待实现 |
| **历史** | 历史列表 | GET | `/history` | 🔄 待实现 |
| **历史** | 历史详情 | GET | `/history/{qaId}` | 🔄 待实现 |
| **历史** | 删除历史 | DELETE | `/history/{qaId}` | 🔄 待实现 |
| **统计** | 用户统计 | GET | `/statistics/user/{userId}` | 🔄 待实现 |

---

## 通用规范

### 请求头

```http
Content-Type: application/json
Accept: application/json
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 状态码规范

| HTTP状态码 | 业务码 | 说明 |
|-----------|-------|------|
| 200 | 200 | 请求成功 |
| 400 | 400 | 请求参数错误 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |

---

## 系统接口

### 1. 健康检查

**接口描述**: 检查系统是否正常运行

```http
GET /api/system/health
```

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "healthy",
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 2. 系统状态

**接口描述**: 获取系统详细状态信息

```http
GET /api/system/status
```

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "系统运行正常",
  "data": {
    "application": "RAG QA System",
    "status": "UP",
    "timestamp": "2026-01-09T12:00:00Z",
    "database": "CONNECTED",
    "ollamaUrl": "http://localhost:11434"
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

---

## 文档管理接口

### 1. 上传文档

**接口描述**: 上传PDF、DOCX、PPTX文档

```http
POST /api/documents/upload
Content-Type: multipart/form-data
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 文档文件 (最大50MB) |
| userId | String | 是 | 用户ID |
| description | String | 否 | 文档描述 |

**支持格式**: PDF (.pdf), Word (.docx), PowerPoint (.pptx), 文本 (.txt)

**响应示例** (成功):
```json
{
  "code": 200,
  "message": "文件已上传，开始处理",
  "data": {
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "filename": "Java_Concurrency.pdf",
    "fileSize": 2048576,
    "fileType": "PDF",
    "status": "PROCESSING",
    "chunkCount": 0,
    "uploadedAt": "2026-01-09T12:00:00Z",
    "description": "Java并发编程教材"
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

**响应示例** (失败):
```json
{
  "code": 400,
  "message": "文件大小超过50MB限制",
  "data": null,
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 2. 文档列表

**接口描述**: 获取用户的所有文档

```http
GET /api/documents?userId={userId}&page={page}&pageSize={pageSize}&status={status}
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认10 |
| status | String | 否 | 文档状态过滤 |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 25,
    "page": 1,
    "pageSize": 10,
    "records": [
      {
        "documentId": "doc_001",
        "filename": "Java_Concurrency.pdf",
        "fileSize": 2048576,
        "fileType": "PDF",
        "status": "SUCCESS",
        "chunkCount": 45,
        "uploadedAt": "2026-01-09T12:00:00Z",
        "processedAt": "2026-01-09T12:01:30Z",
        "description": "Java并发编程教材"
      }
    ]
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 3. 文档详情

**接口描述**: 获取单个文档的详细信息

```http
GET /api/documents/{documentId}
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| documentId | String | 是 | 文档ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "documentId": "doc_001",
    "filename": "Java_Concurrency.pdf",
    "fileSize": 2048576,
    "fileType": "PDF",
    "status": "SUCCESS",
    "chunkCount": 45,
    "uploadedAt": "2026-01-09T12:00:00Z",
    "processedAt": "2026-01-09T12:01:30Z",
    "description": "Java并发编程教材",
    "chunks": [
      {
        "chunkId": "chunk_001",
        "chunkIndex": 1,
        "content": "Java并发编程是现代软件开发中的重要技术...",
        "pageNum": 1,
        "wordCount": 256
      }
    ]
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 4. 删除文档

**接口描述**: 删除文档及其所有相关数据

```http
DELETE /api/documents/{documentId}
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| documentId | String | 是 | 文档ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "文档删除成功",
  "data": {
    "documentId": "doc_001",
    "deletedChunks": 45,
    "deletedVectors": 45
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

---

## 问答接口

### 1. 智能问答

**接口描述**: 基于上传文档进行智能问答

```http
POST /api/qa/ask
Content-Type: application/json
```

**请求参数**:
```json
{
  "userId": "user_123",
  "question": "什么是多线程？",
  "topK": 5,
  "temperature": 0.7
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |
| question | String | 是 | 用户问题 |
| topK | Integer | 否 | 检索相关文档数量，默认5 |
| temperature | Float | 否 | LLM生成温度，默认0.7 |

**响应示例**:
```json
{
  "code": 200,
  "message": "问答成功",
  "data": {
    "qaId": "qa_550e8400",
    "question": "什么是多线程？",
    "answer": "多线程是指一个程序中的多个执行流。在操作系统的调度下，这些线程可以并发执行，从而提高程序的执行效率。\n\n在Java中，多线程主要通过以下方式实现：\n1. 继承Thread类\n2. 实现Runnable接口\n3. 使用线程池\n\n多线程的优势包括：\n- 提高程序响应性\n- 充分利用多核CPU\n- 改善用户体验",
    "responseTime": 2340,
    "citations": [
      {
        "citationId": "cit_001",
        "documentId": "doc_123",
        "documentName": "Java_Concurrency.pdf",
        "pageNum": 15,
        "chunkIndex": 8,
        "relevanceScore": 0.92,
        "chunkContent": "多线程是指在同一程序中同时运行多个线程。在操作系统的调度下，这些线程可以并发执行..."
      },
      {
        "citationId": "cit_002",
        "documentId": "doc_123",
        "documentName": "Java_Concurrency.pdf",
        "pageNum": 18,
        "chunkIndex": 10,
        "relevanceScore": 0.88,
        "chunkContent": "Java中通过Thread类或Runnable接口实现多线程。Thread类提供了创建和控制线程的方法..."
      }
    ]
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

---

## 历史记录接口

### 1. 历史列表

**接口描述**: 获取用户的问答历史记录

```http
GET /api/history?userId={userId}&page={page}&pageSize={pageSize}&keyword={keyword}
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认10 |
| keyword | String | 否 | 搜索关键词 |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 25,
    "page": 1,
    "pageSize": 10,
    "records": [
      {
        "qaId": "qa_001",
        "question": "什么是多线程？",
        "answer": "多线程是指...",
        "responseTime": 2340,
        "askedAt": "2026-01-09T12:00:00Z",
        "citationCount": 5
      },
      {
        "qaId": "qa_002",
        "question": "Java中如何创建线程？",
        "answer": "Java中创建线程有多种方式...",
        "responseTime": 1890,
        "askedAt": "2026-01-09T11:55:00Z",
        "citationCount": 3
      }
    ]
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 2. 历史详情

**接口描述**: 获取单条问答记录的详细信息

```http
GET /api/history/{qaId}
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| qaId | String | 是 | 问答记录ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "qaId": "qa_001",
    "question": "什么是多线程？",
    "answer": "多线程是指一个程序中的多个执行流...",
    "responseTime": 2340,
    "askedAt": "2026-01-09T12:00:00Z",
    "citations": [
      {
        "citationId": "cit_001",
        "documentId": "doc_123",
        "documentName": "Java_Concurrency.pdf",
        "pageNum": 15,
        "chunkIndex": 8,
        "relevanceScore": 0.92,
        "chunkContent": "多线程是指在同一程序中同时运行多个线程..."
      }
    ]
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

### 3. 删除历史

**接口描述**: 删除单条问答记录

```http
DELETE /api/history/{qaId}
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| qaId | String | 是 | 问答记录ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "历史记录删除成功",
  "data": {
    "qaId": "qa_001",
    "deletedCitations": 5
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

---

## 统计接口

### 1. 用户统计

**接口描述**: 获取用户的统计信息

```http
GET /api/statistics/user/{userId}
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": "user_123",
    "totalDocuments": 15,
    "successDocuments": 13,
    "processingDocuments": 1,
    "failedDocuments": 1,
    "totalQuestions": 89,
    "totalChunks": 567,
    "avgResponseTime": 2450,
    "lastActiveAt": "2026-01-09T12:00:00Z",
    "storageUsed": 52428800,
    "documentsStats": {
      "PDF": 8,
      "DOCX": 5,
      "PPTX": 2
    }
  },
  "timestamp": "2026-01-09T12:00:00Z"
}
```

---

## 错误码说明

### 通用错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 请求成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权访问 | 检查用户身份验证 |
| 403 | 禁止访问 | 检查用户权限 |
| 404 | 资源不存在 | 检查请求路径和资源ID |
| 500 | 服务器内部错误 | 联系技术支持 |

### 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1001 | 文件格式不支持 | 使用PDF、DOCX、PPTX格式 |
| 1002 | 文件大小超限 | 文件大小不超过50MB |
| 1003 | 文档处理失败 | 检查文件是否损坏 |
| 1004 | 向量化服务不可用 | 检查HuggingFace API连接 |
| 1005 | LLM服务不可用 | 检查Ollama服务状态 |
| 1006 | 数据库连接失败 | 检查PostgreSQL连接 |

---

## 前端调用示例

### JavaScript/前端调用示例

#### 1. 系统健康检查

```javascript
async function checkSystemHealth() {
    try {
        const response = await fetch('http://localhost:8080/api/system/health');
        const result = await response.json();
        console.log('系统状态:', result);
        return result;
    } catch (error) {
        console.error('健康检查失败:', error);
        throw error;
    }
}
```

#### 2. 上传文档

```javascript
async function uploadDocument(file, userId, description = '') {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);
    if (description) {
        formData.append('description', description);
    }

    try {
        const response = await fetch('http://localhost:8080/api/documents/upload', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        console.log('上传结果:', result);
        return result;
    } catch (error) {
        console.error('文档上传失败:', error);
        throw error;
    }
}
```

#### 3. 智能问答

```javascript
async function askQuestion(userId, question, topK = 5, temperature = 0.7) {
    try {
        const response = await fetch('http://localhost:8080/api/qa/ask', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId,
                question,
                topK,
                temperature
            })
        });
        
        const result = await response.json();
        console.log('问答结果:', result);
        return result;
    } catch (error) {
        console.error('问答失败:', error);
        throw error;
    }
}
```

#### 4. 获取文档列表

```javascript
async function getDocuments(userId, page = 1, pageSize = 10, status = '') {
    const params = new URLSearchParams({
        userId,
        page: page.toString(),
        pageSize: pageSize.toString()
    });
    
    if (status) {
        params.append('status', status);
    }

    try {
        const response = await fetch(`http://localhost:8080/api/documents?${params}`);
        const result = await response.json();
        console.log('文档列表:', result);
        return result;
    } catch (error) {
        console.error('获取文档列表失败:', error);
        throw error;
    }
}
```

#### 5. 获取历史记录

```javascript
async function getHistory(userId, page = 1, pageSize = 10, keyword = '') {
    const params = new URLSearchParams({
        userId,
        page: page.toString(),
        pageSize: pageSize.toString()
    });
    
    if (keyword) {
        params.append('keyword', keyword);
    }

    try {
        const response = await fetch(`http://localhost:8080/api/history?${params}`);
        const result = await response.json();
        console.log('历史记录:', result);
        return result;
    } catch (error) {
        console.error('获取历史记录失败:', error);
        throw error;
    }
}
```

### 前端工具类封装

```javascript
class RAGApiClient {
    constructor(baseUrl = 'http://localhost:8080/api') {
        this.baseUrl = baseUrl;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const result = await response.json();
            
            if (result.code !== 200) {
                throw new Error(result.message || '请求失败');
            }
            
            return result;
        } catch (error) {
            console.error(`API请求失败 [${endpoint}]:`, error);
            throw error;
        }
    }

    // 系统接口
    async checkHealth() {
        return this.request('/system/health');
    }

    async getSystemStatus() {
        return this.request('/system/status');
    }

    // 文档接口
    async uploadDocument(file, userId, description) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('userId', userId);
        if (description) formData.append('description', description);

        return this.request('/documents/upload', {
            method: 'POST',
            body: formData,
            headers: {} // 清空Content-Type，让浏览器自动设置
        });
    }

    async getDocuments(userId, page = 1, pageSize = 10, status = '') {
        const params = new URLSearchParams({ userId, page, pageSize });
        if (status) params.append('status', status);
        return this.request(`/documents?${params}`);
    }

    async deleteDocument(documentId) {
        return this.request(`/documents/${documentId}`, { method: 'DELETE' });
    }

    // 问答接口
    async askQuestion(userId, question, topK = 5, temperature = 0.7) {
        return this.request('/qa/ask', {
            method: 'POST',
            body: JSON.stringify({ userId, question, topK, temperature })
        });
    }

    // 历史接口
    async getHistory(userId, page = 1, pageSize = 10, keyword = '') {
        const params = new URLSearchParams({ userId, page, pageSize });
        if (keyword) params.append('keyword', keyword);
        return this.request(`/history?${params}`);
    }

    async deleteHistory(qaId) {
        return this.request(`/history/${qaId}`, { method: 'DELETE' });
    }

    // 统计接口
    async getUserStatistics(userId) {
        return this.request(`/statistics/user/${userId}`);
    }
}

// 使用示例
const apiClient = new RAGApiClient();

// 检查系统状态
apiClient.checkHealth().then(result => {
    console.log('系统健康:', result);
});

// 上传文档
const fileInput = document.getElementById('fileInput');
fileInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (file) {
        try {
            const result = await apiClient.uploadDocument(file, 'user_123', '测试文档');
            console.log('上传成功:', result);
        } catch (error) {
            console.error('上传失败:', error);
        }
    }
});
```

---

## 开发注意事项

### 1. CORS配置
后端已配置CORS，支持跨域请求。前端可以直接调用API。

### 2. 文件上传限制
- 最大文件大小：50MB
- 支持格式：PDF、DOCX、PPTX、TXT
- 建议使用FormData进行文件上传

### 3. 错误处理
- 统一使用try-catch处理异步请求
- 根据返回的code字段判断请求是否成功
- 显示用户友好的错误信息

### 4. 性能优化
- 问答接口响应时间通常在2-5秒
- 大文件上传可能需要较长时间
- 建议添加加载状态提示

### 5. 数据格式
- 所有时间字段使用ISO 8601格式
- 文件大小以字节为单位
- 相关性分数范围为0-1

---

**文档维护**: 如有接口