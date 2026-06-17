# OpsDesk

OpsDesk is a cloud-ready IT service desk web application for managing support tickets, users, roles, comments, and basic incident workflows.

The goal of this project is to demonstrate a realistic Junior+ / borderline Mid-Level fullstack application with a Java Spring Boot backend, React TypeScript frontend, PostgreSQL database, tests, Docker Compose, and GitHub Actions CI.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit
- Testcontainers

### Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form
- Zod
- Axios or a small fetch wrapper

### DevOps

- Dockerfile for backend
- Dockerfile for frontend
- Docker Compose
- PostgreSQL container
- GitHub Actions CI

## Planned Features

- Ticket creation and management
- Ticket status and priority workflow
- User roles: `ADMIN`, `AGENT`, `REQUESTER`
- Authentication with JWT
- Comments on tickets
- Basic audit history
- Filtering and pagination
- Docker-based local setup
- Automated tests and CI pipeline

## Project Structure

```text
opsdesk/
  backend/
  frontend/
  README.md
  PROJECT_RULES.md
```

## Local Setup

Implementation is planned in small feature branches. The first working version will start with the Spring Boot backend foundation.

Later, the full application should run with:

```bash
docker compose up --build
```

## Development Roadmap

1. Backend foundation
2. Ticket API
3. Auth and roles
4. Comments and basic audit
5. Frontend skeleton
6. Ticket UI
7. Docker Compose
8. GitHub Actions CI
9. Tests
10. README polish and screenshots

