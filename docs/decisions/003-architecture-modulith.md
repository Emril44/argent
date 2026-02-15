# ADR-003: Architecture — Modular Monolith with Explicit Module Boundaries

Date: 2026-02-15

Status: Accepted

Project: Argent

## Context
Argent is a single-developer system focused on transaction integrity. The project needs clear separation of concerns without introducing distributed system overhead.

## Decision
Argent will be implemented as a **modular monolith** with explicit boundaries:
- API layer
- Application layer
- Domain layer
- Infrastructure layer

Optionally, Spring Modulith may be used to enforce module boundaries and support module-level testing and application events.

## Rationale
- Preserves architectural clarity and domain integrity.
- Avoids operational complexity of microservices (network calls, service discovery, distributed tracing, distributed transactions).
- Matches V1 scale and deployment needs.

## Consequences
Positive:
- Clear dependencies and testable boundaries.
- Single deployable unit and simpler deployment.

Negative:
- Does not model production distributed failure modes (acceptable for V1).

## Alternatives Considered
- Microservices: rejected due to operational complexity without V1 benefit.
