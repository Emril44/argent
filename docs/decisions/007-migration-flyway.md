# ADR-007: Schema Migrations — Flyway

Date: 2026-02-15  

Status: Accepted  

Project: Argent

## Context

Argent’s schema will evolve over time (users, wallets, transactions, idempotency, audit logs, indexes). The project requires:

- Deterministic schema creation from scratch
- Traceable, reviewable schema changes over time
- A workflow compatible with production-style deployments
- Minimal tool ceremony (solo developer scope)

Relying on ORM auto-DDL (e.g., Hibernate `ddl-auto=update`) can lead to uncontrolled or environment-dependent schema changes and does not provide an auditable change history.

## Decision

Argent will use **Flyway** for database schema migrations, using **versioned SQL migration scripts**.

- Migrations will be stored in the repository (e.g., `src/main/resources/db/migration/`).
- Migrations are forward-only: applied migrations are not edited; changes are made via new versioned scripts.

## Rationale

- **Deterministic schema evolution:** Migration history is explicit and reproducible across environments.
- **Reviewable artifacts:** Schema changes become part of code review and documentation.
- **SQL-first clarity:** Explicit DDL supports integrity-focused design (constraints, indexes, locking-relevant schema choices).
- **Low ceremony:** Flyway offers a simple, linear workflow suitable for a solo project.

## Consequences

### Positive
- Reliable dev/prod parity: a fresh database can be migrated to the current schema consistently.
- Clear record of schema evolution; reduces “it works on my machine” drift.
- Encourages intentional schema design aligned with auditability and integrity requirements.

### Negative
- Requires maintaining migration scripts manually.
- Rollbacks are not automatic in all cases; typical recovery is via forward-fix migrations.
- Slight overhead when iterating on schema early (mitigated by good migration hygiene).

## Alternatives Considered

1. **Hibernate/JPA auto-DDL (`ddl-auto`)**
   - Rejected because it can introduce uncontrolled schema drift and does not provide a reviewable migration history.

2. **Liquibase**
   - Viable and feature-rich (formats, diffs, rollbacks), but rejected for V1 due to higher complexity relative to Flyway’s simple linear workflow.

## Notes

- Migration naming convention: `V<version>__<description>.sql` (e.g., `V1__init.sql`).
- PostgreSQL is the primary target database (see ADR-006).
