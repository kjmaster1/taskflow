# Taskflow

A production-grade multi-tenant task management REST API built with Java and Spring Boot. Users can register, create projects, and manage tasks with automated workflow enforcement — similar to the backend of a tool like Jira or Linear.

Live API: https://taskflow-production-7b8c.up.railway.app

---

## Tech Stack

- **Java 21** + **Spring Boot 3.5**
- **PostgreSQL** with **Flyway** versioned migrations
- **Spring Security** with stateless **JWT** authentication
- **JUnit 5** + **Mockito** + **MockMvc** (37 tests)
- **Docker** + **Docker Compose**
- **GitHub Actions** CI/CD
- Deployed on **Railway**

---

## Architecture

Feature-based package structure — each domain concept owns its own controller, service, repository, and DTOs:

```
com.kjmaster.taskflow/
├── user/
├── project/
├── task/
├── security/
└── exception/
```

Entities never leave the service layer — all API responses use DTOs to decouple the API contract from the database model and prevent circular serialisation.

---

## API Endpoints

All endpoints except `/users/register` and `/users/login` require an `Authorization: Bearer <token>` header.

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/users/register` | Public | Register a new user |
| POST | `/users/login` | Public | Login and receive JWT token |
| POST | `/projects` | Required | Create a project |
| GET | `/projects` | Required | Get all projects for authenticated user |
| PATCH | `/projects/{id}` | Required | Update a project |
| DELETE | `/projects/{id}` | Required | Delete a project |
| POST | `/projects/{id}/tasks` | Required | Create a task |
| GET | `/projects/{id}/tasks` | Required | Get all tasks for a project |
| PATCH | `/projects/{id}/tasks/{taskId}` | Required | Update a task |
| DELETE | `/projects/{id}/tasks/{taskId}` | Required | Delete a task |

---

## Workflow Engine

Tasks follow a strict state machine — arbitrary status changes are rejected with a 409 Conflict response.

```
TODO → IN_PROGRESS → DONE
```

- Transitioning to `IN_PROGRESS` records a `startedAt` timestamp
- Transitioning to `DONE` records a `completedAt` timestamp
- Backwards transitions and skipped steps are forbidden
- Transition logic lives in the entity (rich domain model), making it impossible to bypass via direct field mutation — `setStatus` is private

---

## Key Design Decisions

**State machine over arbitrary status changes** — allowing clients to set any status freely would break data integrity and make timestamp tracking unreliable. The state machine enforces valid transitions at the domain level.

**Flyway over `ddl-auto=update`** — Hibernate's auto DDL makes unauditable decisions about your schema. Flyway gives explicit, versioned, reversible migrations that are safe in production. Schema changes are code.

**Rich domain model** — business logic lives in entities rather than services. The `Task` entity owns its transition logic, meaning there is no way to change task status without going through `transitionTo()`.

**DTO pattern** — entities are never serialised directly. DTOs prevent circular references, hide internal structure, and decouple the API contract from the database model.

**Dual cascade strategy** — `CascadeType.ALL` in JPA keeps Hibernate's in-memory state consistent and allows lifecycle callbacks to fire. `ON DELETE CASCADE` in SQL ensures integrity even when the database is accessed directly, bypassing the application layer.

---

## Running Locally

**Prerequisites:** Docker and Docker Compose

**1. Clone the repository**
```bash
git clone https://github.com/kjmaster1/taskflow
cd taskflow
```

**2. Create a `.env` file in the project root**
```
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
```

**3. Start the application**
```bash
docker compose up --build
```

The API will be available at `http://localhost:8081`. Flyway will automatically create all database tables on first run.

---

## Running Tests

```bash
mvn test
```

37 tests across unit and integration layers:
- Service layer unit tests with Mockito
- Controller integration tests with MockMvc and H2 in-memory database
- Full Spring context loaded for integration tests via `@SpringBootTest`

---

## CI/CD

GitHub Actions runs the full test suite on every push and pull request to `main`. See `.github/workflows/ci.yml`.
