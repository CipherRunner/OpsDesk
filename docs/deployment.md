# Deployment Preparation

OpsDesk is not currently deployed by this repository. This document captures the minimum configuration needed for a future staging or production-like deployment without adding cloud infrastructure, registry credentials, Kubernetes, Terraform, or a monitoring stack.

## Deployment Shape

- Run PostgreSQL 16 as the persistent database.
- Run the backend as a Spring Boot container listening on port `8080`.
- Run the frontend as an Nginx container listening on port `80`.
- Let the frontend proxy `/api` requests to the backend service.
- Let Flyway run database migrations during backend startup.
- Keep demo data disabled for production-like environments.

The provided `docker-compose.prod.yml` is intended as a simple production-like reference for a single Docker host. It builds local images and does not push anything to a registry.

## Required Environment Variables

For `docker-compose.prod.yml`, set these values outside the repository before running the stack:

| Variable | Required | Notes |
| --- | --- | --- |
| `POSTGRES_PASSWORD` | Yes | Secret password for PostgreSQL and the backend datasource. |
| `OPS_DESK_JWT_SECRET` | Yes | Secret used to sign JWTs. Use a long random value and do not use the development fallback. |
| `POSTGRES_DB` | No | Database name. Defaults to `opsdesk`. |
| `POSTGRES_USER` | No | Database user. Defaults to `opsdesk`. |
| `FRONTEND_PORT` | No | Host port for the frontend container. Defaults to `3000`. |
| `SPRING_PROFILES_ACTIVE` | No | Defaults to `docker` in the production-like compose file. |

If the backend is deployed outside Compose, provide the equivalent Spring datasource variables directly:

| Variable | Required | Notes |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Yes | JDBC URL for the PostgreSQL database. |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database username. |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password. Keep it secret. |
| `OPS_DESK_JWT_SECRET` | Yes | Long random JWT signing secret. Keep it secret. |
| `OPS_DESK_DEMO_DATA_ENABLED` | Recommended | Set to `false` for staging or production-like environments. |

## Secret Handling

- Do not commit real `.env`, `.env.production`, registry credentials, database passwords, JWT secrets, or cloud credentials.
- The repository `.gitignore` excludes `.env` and `.env.*`, except for `.env.example`.
- Store real values in the deployment environment, a local untracked env file, or a secrets manager managed outside this repository.
- Use temporary dummy values only when validating Compose syntax locally.

## Production-Like Startup Assumptions

- Docker and Docker Compose are available on the target host.
- The database volume is persistent and backed up by host-level processes outside this repository.
- TLS termination, domain routing, and any public reverse proxy are provided outside this Compose file.
- The backend can reach PostgreSQL at the configured datasource URL.
- The frontend is served from the same origin as `/api`, or an external reverse proxy preserves that path.
- `/actuator/health` remains available for a basic backend health check.
- With demo data disabled, initial users must be created through the existing application flow.

## Local Validation

Validate the default local Compose file:

```bash
docker compose config
```

Validate the production-like Compose file with temporary local values:

```powershell
$env:POSTGRES_PASSWORD = '<local-validation-only>'
$env:OPS_DESK_JWT_SECRET = '<local-validation-only-long-random-string>'
docker compose -f docker-compose.prod.yml config
Remove-Item Env:\POSTGRES_PASSWORD
Remove-Item Env:\OPS_DESK_JWT_SECRET
```

Run the existing checks before using any deployment artifact:

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run build
```

On Unix-like shells, run the Maven wrapper as `./mvnw test` from `backend/`.
