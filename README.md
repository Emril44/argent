# argent
Backend-focused financial transaction integrity simulator modeling atomic transfers, idempotency, pessimistic locking, and financial invariants using a modular monolith architecture.

## Core Principles

- Monetary precision via `BigDecimal` with fixed scale and explicit rounding 
- Atomic transfers with pessimistic locking and deterministic lock ordering to prevent deadlocks 
- Idempotent transfer requests via client-generated UUID keys 
- Immutable transaction history 
- Domain-driven design with strict separation between persistence and domain layers

## Architecture

**Modular monolith**:
- _Spring Boot_ backend
- _PostgreSQL_
- _Flyway_ migrations

Packages: `user`, `wallet`, `transaction`, `money`, `notification`

Each domain package owns its model, entity, repository, service, and controller. JPA entities are kept separate from domain objects to preserve constructor-enforced invariants.

## Stack
- _Java 21_, _Spring Boot 3.2_ 
- _PostgreSQL 16_
- _Flyway_ (schema migrations)
- _Testcontainers_ (integration tests)
- _Docker Compose_

## Run Locally
**Prerequisites:**
- _Docker Desktop_

**Start the stack**

`docker compose up --build`

This starts _PostgreSQL_ and the _backend_:
- API: `http://localhost:8080`
- Database: `http://localhost:5433`

**Reset the database**
```
docker compose down -v
docker compose up --build
```
_Flyway_ will reapply all migrations on startup.

## API Endpoints
**Authentication is stubbed in V0.1** - pass `userId` as a request header where required. **_Spring Security_ is planned for V0.2.**

### Users

| Method | Endpoint         | Description         |
|--------|------------------|---------------------|
| POST   | `/users/register` | Register a new user |

### Wallets

| Method | Endpoint                       | Description        |
|--------|--------------------------------|--------------------|
| POST   | `/wallets`                     | Create a wallet    |
| GET    | `/wallets/{walletId}`          | Get wallet details |
| PATCH  | `/wallets/{walletId}/freeze`   | Freeze a wallet    |
| PATCH  | `/wallets/{walletId}/unfreeze` | Unfreeze a wallet  |

### Transactions

| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| POST   | `/transactions/deposit`         | Deposit funds            |
| POST   | `/transactions/withdraw`        | Withdraw funds           |
| POST   | `/transactions/transfer`        | Transfer between wallets |
| GET    | `/transactions/{transactionId}` | Fetch transaction by ID  |

## Notes
- Transfer requests require an `idempotencyKey` (client-generated UUID) - retrying with the same key returns the original transaction without re-processing 
- `userId` passed as request header for wallet and transaction endpoints (temporary - V0.2 replaces with session auth)

## Testing
Integration tests run against a real _PostgreSQL_ instance via _Testcontainers_.

Concurrency test suite uses `ExecutorService` and `CountDownLatch` to manufacture simultaneous operations and verify locking correctness.

> **Windows note:** If running tests locally, create `C:\Users\<you>\.docker-java.properties` containing `api.version=1.44` to resolve _Docker API_ version negotiation with _Docker Desktop._

## Status
**V0.1 complete.** Core backend feature-complete - domain modeling, persistence, concurrency control, idempotency, event-driven notifications, REST API, containerised deployment.


**V0.2 planned:** _Spring Security_ (session auth, _BCrypt_), frontend (_React_), observability (_Prometheus + Grafana_), CI/CD pipeline, cloud deployment.
