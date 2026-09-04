# DMS — Document Management System

An enterprise Document Management System with multi-level approval workflows, organizational
hierarchy management, document version control, in-app notifications, and a full audit trail.

---

## Project Overview

**What is DMS?**
DMS is a web application for managing the full lifecycle of business documents — upload,
review, approval, versioning, and archival — with the approvals routed automatically through
an organization's reporting hierarchy.

**The problem it solves**
Organizations lose track of who approved what, when, and why. Approval requests get stuck in
inboxes, document versions get emailed around, and there is no reliable record of the decision
trail. DMS centralizes documents, drives approvals through a configurable workflow tied to the
manager hierarchy, notifies the right people at each step, and records every state change in an
immutable audit log.

**Key features**
- JWT-authenticated REST API with role-based access control (`ADMIN`, `USER`, `VIEWER`)
- User, department, and reporting-hierarchy management (assign / reassign / remove managers)
- Document upload with metadata, categories, version history, archive / restore, and download
- Configurable approval workflows (ordered steps, per-step approval level, timeouts)
- Workflow execution: submit → approve / reject / send-back / escalate, with the approver
  resolved from the submitter's manager chain
- In-app notifications for every workflow event and document comment
- Notification preferences per user (per-event, in-app / email toggles)
- Immutable audit log for all state-changing operations, with old/new value snapshots
- Dashboard statistics and scheduled daily jobs (approval reminders, pending-email flush)

---

## Tech Stack

### Backend
| Concern | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Security | Spring Security + JWT (`io.jsonwebtoken` 0.12.3) |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Migrations | Flyway 10 |
| DTO mapping | MapStruct 1.5 |
| API docs | springdoc-openapi (Swagger UI) 2.3 |
| Build | Maven 3.9+ |

### Frontend
| Concern | Choice |
|---|---|
| Framework | React 18 |
| Build tool | Vite 5 |
| Styling | Tailwind CSS 3.4 |
| Routing | React Router 6 |
| HTTP | Axios |
| Notifications | react-hot-toast |
| Icons | lucide-react |

---

## Architecture Overview

### Request flow

```
Client (React SPA)
      │  Authorization: Bearer <JWT>
      ▼
Controller  ──  @PreAuthorize RBAC, request validation
      ▼
Service (impl)  ──  business rules, transactions, cross-cutting
      │                calls NotificationService + AuditService
      ▼
Repository (Spring Data JPA)
      ▼
MySQL   (schema owned entirely by Flyway; ddl-auto = validate)
```

### Authentication flow

1. `POST /api/v1/auth/login` with email + password.
2. Server validates credentials and returns a signed JWT (`HS512`) plus the user profile.
3. The SPA stores the token and sends it as `Authorization: Bearer <token>` on every request.
4. A security filter validates the token on each request and populates the security context
   with the user's authority (`ROLE_ADMIN` / `ROLE_USER` / `ROLE_VIEWER`).
5. `@PreAuthorize` expressions on controllers enforce role and ownership rules
   (e.g. `@securityUtils.isAdminOrSelf(#userId)`, `@securityUtils.isCurrentApprover(#id)`).
6. On `401` the SPA clears the token and redirects to `/login`.

> **Note:** `server.servlet.context-path` is **not** set. All paths already include the
> `/api/v1` prefix (see `ApiConstants`), and Swagger is served at the server root.

---

## Setup Instructions

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.9+
- Node.js 18+

### 1. Database

```sql
CREATE DATABASE dms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Flyway creates and seeds every table on the first backend start — do not create tables by hand.

### 2. Backend

```bash
cd dms-backend

# Configure connection (dev profile has localhost fallbacks; override as needed)
export DB_URL="jdbc:mysql://localhost:3306/dms_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME=root
export DB_PASSWORD=your_password

mvn spring-boot:run                       # runs with the default 'dev' profile
```

The API is now at `http://localhost:8080/api/v1`.

To run the production profile (requires all env vars, no fallbacks):

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=... DB_USERNAME=... DB_PASSWORD=...
export JWT_SECRET=<base64-256-bit-plus-secret>
mvn spring-boot:run
```

### 3. Frontend

```bash
cd dms-frontend
npm install

# optional — defaults to http://localhost:8080/api/v1
echo "VITE_API_BASE_URL=http://localhost:8080/api/v1" > .env.local

npm run dev                               # http://localhost:5173
```

---

## Default Admin Credentials

| Field | Value |
|---|---|
| Email | `admin@dms.com` |
| Password | `Admin@123` |

Seeded by Flyway (`V1.1`). **Change this immediately in any shared or production environment.**

---

## API Documentation

Interactive Swagger UI (backend running):

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

---

## Project Structure

```
DMS/
├── .gitignore
├── README.md
├── dms-backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/dms/
│       │   ├── config/         # Security, JWT, CORS, OpenAPI, JPA auditing
│       │   ├── constant/       # ApiConstants, RoleConstants, ErrorConstants
│       │   ├── controller/     # REST endpoints (thin; @PreAuthorize here)
│       │   ├── dto/            # request/ + response/ payloads
│       │   ├── entity/         # JPA entities (BaseEntity = audit columns)
│       │   ├── exception/      # domain exceptions + GlobalExceptionHandler
│       │   ├── mapper/         # MapStruct entity <-> DTO mappers
│       │   ├── repository/     # Spring Data JPA repositories
│       │   ├── scheduler/      # @Scheduled jobs (NotificationScheduler)
│       │   ├── service/        # interfaces + impl/ (business logic)
│       │   └── util/           # SecurityUtils, AuditHelper, ...
│       └── resources/
│           ├── application.yml         # profile-independent base config
│           ├── application-dev.yml     # local dev (localhost fallbacks)
│           ├── application-prod.yml    # production (env-only, no fallbacks)
│           └── db/migration/           # Flyway V1..V6.x
└── dms-frontend/
    └── src/
        ├── api/          # axios instance + per-domain API modules
        ├── components/   # ui/ (primitives) + common/ + layout/
        ├── context/      # AuthContext
        ├── hooks/        # useAuth
        ├── pages/        # one folder per feature area
        └── routes/       # AppRouter, RouteConstants, PrivateRoute
```

---

## Project Status

| Phase | Scope | Status |
|---|---|---|
| 1 | Foundation & Security — auth, JWT, users, departments, RBAC | ✅ |
| 2 | Hierarchy & Workflow — reporting chains, workflow definitions/steps | ✅ |
| 3 | Document Management — upload, versions, categories, archive | ✅ |
| 4 | Workflow Execution — submit / approve / reject / send-back / escalate | ✅ |
| 5 | Notifications, Audit & Dashboard — in-app notifications, audit log, stats | ✅ |

Post-audit remediation (frontend build fixes, notification/audit pipeline wiring,
`@EnableScheduling`, escalation authorization, workflow-name uniqueness, `VIEWER` role,
audit columns, config split) is applied on top of the above.

---

## Contributing Guidelines

- **Follow existing patterns.** Match the surrounding code's structure, naming, and layering
  (Controller → Service interface + impl → Repository).
- **Schema changes go through Flyway.** Add a new `V<n>__description.sql` migration; never edit
  an applied migration and never rely on `ddl-auto` (it is `validate`). Migrations target MySQL
  and should be written to be re-runnable.
- **DTO mapping uses MapStruct.** Add mapper methods; don't hand-roll conversion in services.
- **Authorization uses `@PreAuthorize`** on controllers, with `RoleConstants` expressions or
  `@securityUtils.*` helpers for ownership checks. Services must not assume the caller is allowed.
- **State-changing operations must** create an audit entry (`AuditService.logAction`) and, where
  a user should be told, a notification (`NotificationService`). Use the `safeAudit` / `safeNotify`
  helper pattern already present in the service impls.
- **`created_by` / `updated_by` are `VARCHAR(100)`** (the actor's email), consistent with
  `BaseEntity` — not numeric user ids.
- Keep secrets out of committed config. `application-prod.yml` has no fallback values.

---

## License

Proprietary. All rights reserved. Not licensed for redistribution or external use.
