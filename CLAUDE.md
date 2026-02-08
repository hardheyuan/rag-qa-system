# CLAUDE.md

This file gives coding context for this repository.

## Project Overview

RAG QA System for educational knowledge bases.

- Backend: Spring Boot 3.2, Java 21, Maven
- Frontend: Vue 3, Vite, Pinia, Tailwind CSS
- Database: PostgreSQL (Supabase) + pgvector

## Common Commands

### Backend

```bash
cd demo1
mvn spring-boot:run
mvn clean package
mvn test
```

### Frontend

```bash
cd frontend
npm install
npm run dev
npm run test
npm run build
```

## Database Notes

- Database connection is configured in `demo1/src/main/resources/application.yml`.
- Flyway migrations live in `demo1/src/main/resources/db/migration/`.
- Naming: `V{version}__{description}.sql`.

## Troubleshooting

- Backend start failure: verify datasource URL/username/password in `application.yml`.
- Flyway failure: run `mvn flyway:info` and check migration version/checksum.
- Frontend API issues: confirm backend is running on `http://localhost:8080`.
