# ADR-006: Database Selection — PostgreSQL

Date: 2026-02-15  

Status: Accepted  

Project: Argent

## Context

Argent models transaction integrity under concurrent access (atomic transfers, pessimistic locking, idempotency, auditability). The persistence layer must support:

- Reliable ACID transactions
- Predictable row-level locking behavior for balance updates
- Strong relational constraints (FKs, unique constraints) to enforce integrity
- Production-like behavior suitable for learning and deployment

## Decision

Argent will use **PostgreSQL** as the primary relational database for development and production.

## Rationale

- **Transaction integrity and locking:** PostgreSQL provides robust transaction semantics and row-level locking behavior suitable for pessimistic locking strategies used to prevent double-spend scenarios.
- **Strong integrity constraints:** PostgreSQL supports rich constraints (foreign keys, unique constraints, check constraints) that complement application-level invariants.
- **Operational practicality:** Easy local setup via Docker, widespread support in hosting environments, and strong tooling for inspection/debugging.
- **Learning alignment:** Matches common production patterns for integrity-focused systems and enables realistic concurrency testing.

## Consequences

### Positive
- High confidence in correctness under concurrent balance updates.
- Clear schema modeling with strong relational constraints.
- Good dev/prod parity and reproducible environments (Docker).

### Negative
- Heavier operational footprint than file-based databases (e.g., SQLite).
- Requires managing DB lifecycle locally (handled via Docker compose).

## Alternatives Considered

1. **SQLite**
   - Rejected due to different concurrency/locking characteristics and limited realism for concurrent, multi-user transaction scenarios.

2. **MySQL/MariaDB**
   - Viable alternative, but PostgreSQL is preferred as a correctness-first default for this project’s integrity and constraint-heavy requirements.

3. **In-memory databases (e.g., H2)**
   - Useful for fast tests, but not suitable as the primary database due to behavioral differences from production-grade relational systems.

## Notes

- Database schema is evolved via versioned migrations (see ADR: migrations tool).
- Pessimistic locking approach for balance updates is defined in ADR-002.
