# RAG QA System

教学知识库智能问答系统（RAG）。

## Tech Stack

- Backend: Spring Boot 3.2, Java 21, Maven, Flyway
- Frontend: Vue 3, Vite, Pinia, Tailwind CSS
- Database: PostgreSQL (Supabase) + pgvector

## Project Structure

```
myProject/
├── demo1/                # Spring Boot backend
├── frontend/             # Vue frontend
├── .env.example          # env template
├── CLAUDE.md             # project notes
└── README.md
```

## Quick Start

### 1) Configure database

Current project is streamlined for direct Supabase usage.

Edit `demo1/src/main/resources/application.yml`:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 2) Start backend

```bash
cd demo1
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/api/system/health
```

### 3) Start frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Common Commands

Backend:

```bash
cd demo1
mvn test
mvn clean package
```

Frontend:

```bash
cd frontend
npm run test
npm run build
```

## Notes

- Database schema is managed by Flyway migrations in `demo1/src/main/resources/db/migration/`.
