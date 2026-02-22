# argent
Backend-focused transaction integrity simulator modeling atomic transfers, idempotency, pessimistic locking, and financial invariants using a modular monolith architecture.

## Core Principles

- Monetary precision (BigDecimal Money)
- Atomic transfers
- Pessimistic locking
- Idempotent transfer requests
- Immutable transaction history

## Architecture

- Monetary precision (BigDecimal Money)
- Atomic transfers
- Pessimistic locking
- Idempotent transfer requests
- Immutable transaction history
- Architecture

## Run Locally
### 1. Start PostgreSQL (Docker)
```
docker compost up -d
```

This starts a PostgreSQL 16 container:
- Host: `localhost`
- Port: `5433`
- Database: `argent`
- Username: `argent`
- Password: `argent`

### 2. Run the backend
From the `backend` directory:
```
./mvnw spring-boot:run
```
...or run `ArgentApplication` from your IDE.

### 3. Timezone
The backend runs in UTC to ensure deterministic timestamp handling and database compatibility.

If running manually via JVM:
```
-Duser.timezone=UTC
```
(Already configured for local development.)

### 4. Reset the Database
To wipe the database and start fresh:
```
docker compose down -v
docker compose up -d
```
Flyway will re-apply migrations on startup.

## Status
Currently in active development. Core schema and domain modeling underway.
