# Checkpoint

Checkpoint is a full-stack gaming backlog app where users can:

- browse a shared global library,
- add games to a personal backlog,
- track progress with statuses (for example: `WANT_TO_PLAY`, `IN_PROGRESS`, `COMPLETED`),
- and (for the admin user) manage games in the global library.

The backend is Spring Boot with JWT authentication and MySQL, and the frontend is a static single-page app (SPA) built with jQuery and served by Spring Boot.

## Prerequisites

- Java `25` (as configured in `pom.xml`)
- Maven `3.9+`
- Docker (for local MySQL via `docker-compose.yml`)

## Run Locally

### 1) Start the database

From the project root:

```bash
docker compose up -d db
```

This starts MySQL on `localhost:3307` with database name `checkpoint`.

### 2) Load DB environment variables

The app reads DB credentials from environment variables (`DB_USERNAME`, `DB_PASSWORD`).

```bash
set -a
source .env.local
set +a
```

### 3) Start the application

```bash
mvn spring-boot:run
```

The app runs on `http://localhost:8081` (configured in `src/main/resources/application.yml`).

## API Docs

When the app is running, OpenAPI/Swagger docs are available at:

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Optional: Run with the `dev` profile

If you want auto-seeded dev accounts, run:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Default dev credentials are defined in `src/main/resources/application-dev.yml` (and can be overridden with env vars).

## Stop Local Services

To stop the MySQL container:

```bash
docker compose down
```
