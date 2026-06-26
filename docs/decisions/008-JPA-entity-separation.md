# ADR-007: Entity Separation - JPA

Date: 2026-06-26

Status: Accepted

Project: Argent

## Context
JPA requires a no-arg constructor on mapped classes. Domain objects in Argent enforce invariants at construction time — Money rejects negatives, Wallet starts with a zero balance, User generates its own UUID. Annotating domain classes directly would allow JPA to instantiate them via no-arg constructor, bypassing all invariant enforcement.

## Decision
Separate JPA entity classes (UserEntity, WalletEntity, MoneyEntity) from domain objects. Entities handle persistence concerns only. Bidirectional mapping methods on entity classes translate between layers.

## Rationale
Domain invariants must be unbypassable regardless of how an object is instantiated. Infrastructure concerns belong in the infrastructure layer, not the domain.

## Consequences
**Positive**: domain objects remain pure, invariants always enforced.

**Negative**: mapper layer required, bidirectional User↔Wallet relationship requires two-phase hydration to resolve circular dependency during reconstitution.