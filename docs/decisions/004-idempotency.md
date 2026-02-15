# ADR-004: Idempotency — Storage and Retry Policy for Transfers

Date: 2026-02-15

Status: Accepted

Project: Argent

## Context
Clients may retry requests due to network timeouts or UI resubmissions. Argent must ensure retries do not double-apply balance-changing operations.

## Decision
Transfer requests will support an `Idempotency-Key` and persist idempotency state in the database.

- A unique constraint will be enforced on `(userId, idempotencyKey)`.
- On first processing, the system records the key as `IN_PROGRESS` within the same DB transaction as the transfer.
- If a duplicate request arrives while `IN_PROGRESS`, the system returns **409 Conflict** instructing the client to retry with the same key.
- On success, the record is updated to `SUCCESS` and stores `txId` and a response snapshot.
- Subsequent retries with `SUCCESS` return the stored response without modifying state.

## Rationale
- Guarantees at-most-once effect per key.
- Handles duplicates safely even under concurrent retries.
- Keeps behavior deterministic and auditable.

## Consequences
Positive:
- Prevents double-charging under retries.
- Clear, testable behavior.

Negative:
- Requires idempotency table and lifecycle management.

## Alternatives Considered
- Store only on success: simpler, but weaker under overlapping concurrent retries.
- 202 Accepted for IN_PROGRESS: implies async processing; rejected for V1.
