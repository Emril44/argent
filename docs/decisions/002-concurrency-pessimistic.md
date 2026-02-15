# ADR-002: Concurrency Strategy for Balance-Changing Operations — Pessimistic Locking

Date: 2026-02-15

Status: Accepted

Project: Argent

## Context

Argent models financial transaction integrity under concurrent access. The system must prevent double-spend scenarios and guarantee that wallet balances never become negative (no overdraft in V1). Transfers must be atomic: debit and credit must succeed or fail together.

Concurrent requests may target the same source wallet (e.g., multiple transfers initiated at the same time or duplicate client retries).

## Decision

Argent will use **pessimistic locking** for balance-changing operations involving wallets.

For TRANSFER and WITHDRAW operations:
- Load the source wallet using a database-level lock (e.g., `SELECT ... FOR UPDATE` via ORM pessimistic write lock).
- For TRANSFER, also lock the destination wallet to ensure consistent updates.
- Perform validation (sufficient funds, wallet status) after locks are acquired.
- Apply debit/credit and persist within a single database transaction.

Lock ordering:
- When two wallets are involved (TRANSFER), locks will be acquired in a deterministic order (e.g., ascending walletId) to reduce deadlock risk.

## Rationale

- Ensures **serializable behavior** for updates to the same wallet balance.
- Prevents **double spend** where two concurrent transfers read the same balance and both succeed.
- Prioritizes integrity over throughput, which matches Argent’s purpose and fintech-style correctness constraints.
- Simpler to reason about and test for a V1 modular monolith.

## Consequences

### Positive
- Strong guarantees against race conditions affecting wallet balances.
- Clear and deterministic behavior under concurrency.
- Easier to validate invariants and write concurrency-focused integration tests.

### Negative
- Reduced parallelism when many operations contend for the same wallet.
- Potential for deadlocks if lock ordering is inconsistent (mitigated by deterministic ordering).
- Performance characteristics differ from optimistic locking approaches.

## Alternatives Considered

1. **Optimistic locking (version fields)**
   - Rejected for V1 due to retry complexity and the need to carefully design conflict handling.
   - May be revisited later if throughput becomes a goal.

2. **Relying only on isolation levels**
   - Rejected because isolation alone may not prevent race conditions without explicit locking depending on DB and access patterns.

## Notes

- Balance-modifying operations are executed inside a single DB transaction.
- Idempotency is handled separately (ADR: idempotency storage and retry policy) and does not replace locking requirements.
