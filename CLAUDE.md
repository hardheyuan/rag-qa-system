# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **RAG (Retrieval-Augmented Generation) QA System** - an intelligent question-answering system for educational knowledge bases. The system uses RAG architecture to provide accurate answers based on uploaded documents.

### Tech Stack
- **Backend**: Spring Boot 3.2 + Java 21 + Maven
- **Frontend**: Vue 3 + Vite + Tailwind CSS 4
- **Database**: PostgreSQL 15 + pgvector extension
- **AI/ML**: LangChain4j + ModelScope API (DeepSeek-R1 + Qwen3-Embedding)
- **Authentication**: JWT (Access Token + Refresh Token)

## Common Commands

### Backend (Spring Boot)

```bash
# Navigate to backend directory
cd demo1

# Run the application
mvn spring-boot:run

# Build the project
mvn clean package

# Run tests
mvn test

# Run a specific test class
mvn test -Dtest=AuthControllerTest

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend (Vue 3)

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run tests
npm run test

# Preview production build
npm run preview
```

### Database (Docker)

```bash
# Start PostgreSQL with pgvector
docker-compose up -d postgres

# View logs
docker-compose logs -f postgres

# Stop services
docker-compose down

# Reset database (WARNING: data loss)
docker-compose down -v
```

## Architecture Overview

### Project Structure

```
myProject/
├── demo1/                          # Spring Boot Backend
│   ├── src/main/java/com/hiyuan/demo1/
│   │   ├── config/                 # Configuration classes
│   │   ├── controller/             # REST API controllers
│   │   ├── service/                # Business logic
│   │   ├── repository/             # Data access layer
│   │   ├── entity/                 # JPA entities
│   │   ├── dto/                    # Data transfer objects
│   │   ├── security/               # JWT authentication
│   │   └── exception/              # Custom exceptions
│   ├── src/main/resources/
│   │   ├── application.yml         # Main configuration
│   │   └── db/migration/           # Flyway migrations
│   └── pom.xml                     # Maven dependencies
├── frontend/                       # Vue 3 Frontend
│   ├── src/
│   │   ├── components/             # Vue components
│   │   ├── views/                  # Page components
│   │   ├── stores/                 # Pinia state management
│   │   ├── api/                    # API client
│   │   └── router/                 # Vue Router config
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml              # Docker services
└── 参考文档/                        # Documentation
```

### RAG System Architecture

```
Document Upload Flow:
1. User uploads PDF/DOCX/PPTX/TXT
2. File saved to ./uploads/
3. Document entity created with PROCESSING status
4. Async process begins:
   - Parse document (PDFBox/POI/Aliyun OCR)
   - Clean and chunk text (configurable size/overlap)
   - Generate embeddings via ModelScope API
   - Store vectors in PostgreSQL pgvector
   - Update status to SUCCESS

Question Answering Flow:
1. User asks a question
2. Question is embedded using same embedding model
3. Vector similarity search in pgvector (top-K retrieval)
4. Retrieved chunks form the context
5. LLM (DeepSeek-R1) generates answer with context
6. Response includes answer and citations
```

### Key Configuration (application.yml)

**Database & Vector Store:**
- PostgreSQL with pgvector extension for vector storage
- Supports 1024-dimensional vectors (configurable via MRL)

**AI/LLM Configuration:**
```yaml
langchain4j:
  modelscope:
    api-key: ${MODELSCOPE_API_KEY}
    base-url: https://api-inference.modelscope.cn/v1
    chat-model: deepseek-ai/DeepSeek-R1-0528
    embedding-model: Qwen/Qwen3-Embedding-0.6B
```

**MRL (Matryoshka Representation Learning):**
- Original dimension: 1024
- Target dimension: 1024 (supports 256, 512, 1024, 2048)
- Reduces storage while maintaining semantic quality

**JWT Authentication:**
- Access Token: 30 minutes expiration
- Refresh Token: 7 days expiration
- Configurable via jwt.* properties

**OCR Configuration:**
- Aliyun OCR for scanned PDFs and images
- Cost controls: max 10 pages per PDF
- Trigger threshold: 200 characters minimum

## Development Guidelines

### Code Style
- Java: Use Lombok for boilerplate reduction (@Getter, @Setter, @Builder)
- Vue: Use Composition API with `<script setup>`
- Follow existing package structure

### Testing
- Backend: JUnit 5 + Spring Boot Test
- Frontend: Vitest + Vue Test Utils
- Run tests before committing: `mvn test` and `npm run test`

### Database Migrations
- Use Flyway for database migrations
- Place migration scripts in `demo1/src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`

### Environment Variables
Required environment variables:
- `MODELSCOPE_API_KEY` - ModelScope API access
- `JWT_SECRET` - JWT signing key (min 32 chars)
- `DB_PASSWORD` - PostgreSQL password
- `ALIYUN_OCR_APPCODE` - Aliyun OCR (optional)

### Security Considerations
- JWT tokens for authentication
- Role-based access control (RBAC)
- Input validation on all endpoints
- File upload restrictions (size, type)
- CORS configured for frontend origin

## Troubleshooting

### Common Issues

**Backend won't start:**
- Check PostgreSQL is running: `docker-compose ps`
- Verify database exists: `psql -U postgres -c "\l"`
- Check pgvector extension: `psql -U postgres -d rag_qa_system -c "SELECT * FROM pg_extension WHERE extname = 'vector';"`

**Frontend build errors:**
- Delete node_modules: `rm -rf node_modules package-lock.json`
- Reinstall: `npm install`
- Check Node version: `node --version` (should be 18+)

**API connection errors:**
- Verify backend is running on port 8080
- Check CORS configuration allows frontend origin
- Verify API base URL in frontend config

**Document processing stuck:**
- Check logs: `tail -f demo1/logs/application.log`
- Verify ModelScope API key is set
- Check async executor is configured

## Documentation References

- `参考文档/API-DOCUMENTATION.md` - REST API specification
- `参考文档/BACKEND-ARCHITECTURE.md` - Backend architecture guide
- `参考文档/MRL-README.md` - MRL vector configuration
- `参考文档/COMPLETE-SYSTEM-DESIGN.md` - System design overview
