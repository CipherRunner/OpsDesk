# Project Rules

Project: OpsDesk

## Purpose

OpsDesk is a monolithic IT service desk application. The project should stay focused, understandable, and easy to explain in an interview.

The goal is not to use the maximum number of technologies. The goal is to build a complete, runnable application with clear business logic and a clean fullstack structure.

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- React
- TypeScript
- Vite
- Docker Compose
- GitHub Actions

## Architecture Rules

- Keep the project monolithic.
- Do not introduce microservices.
- Do not expose JPA entities directly from controllers.
- Use DTOs for API input and output.
- Keep business logic in service classes.
- Use constructor injection.
- Add validation to request DTOs.
- Use meaningful package structure.
- Keep controllers thin.
- Use repositories only for persistence access.
- Do not introduce new libraries without a clear reason.
- Do not commit secrets, passwords, tokens, or local `.env` files.

## Backend Package Structure

Base package:

```text
com.mark.opsdesk
```

Planned modules:

```text
com.mark.opsdesk
  user
  ticket
  asset
  security
  audit
  common
```

## Backend Rules

- Controllers handle HTTP requests and responses only.
- Services contain business rules and workflow decisions.
- Repositories access the database.
- Entities model persistence state.
- DTOs define API contracts.
- Validation belongs on request DTOs.
- Exceptions should be converted to a consistent API error response.
- Database changes must be added through Flyway migrations.

## Frontend Rules

- Use React with TypeScript.
- Keep API calls in a dedicated client layer.
- Keep route-level pages separate from reusable components.
- Do not hardcode backend URLs outside the API client configuration.
- Keep UI practical and service-desk focused.
- Avoid over-designed marketing layouts.

## Git Workflow

- Do not work directly in `main`.
- Use feature branches.
- Keep commits small and focused.
- Review the diff before committing.
- Run relevant tests before committing.

Example branches:

```text
feature/project-setup
feature/backend-foundation
feature/ticket-api
feature/auth-roles
feature/comments-audit
feature/frontend-skeleton
feature/ticket-ui
feature/docker-compose
feature/github-actions
feature/tests
feature/readme-polish
```

## Definition of Done

A feature is done when:

- The code compiles.
- Tests pass.
- No unnecessary dependencies were added.
- No secrets are committed.
- Controllers do not expose JPA entities.
- DTOs are clear and meaningful.
- Business logic is not placed in controllers.
- Errors are handled consistently.
- The implementation can be explained in an interview.

