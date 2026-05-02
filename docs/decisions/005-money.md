# ADR-005: Monetary Representation — Money Value Object Using BigDecimal

Date: 2026-02-15

Status: Accepted

Project: Argent

## Context
Financial values require deterministic arithmetic. Floating-point types introduce representation and rounding error.

## Decision
Argent will represent monetary values as a `Money` value object wrapping `BigDecimal`:
- Fixed scale (e.g., 2 for V1)
- Explicit rounding mode (e.g., HALF_UP)
- No direct BigDecimal arithmetic outside Money

## Rationale
- Prevents precision drift.
- Centralizes rounding rules.
- Simplifies invariant enforcement.

## Consequences
- Slight performance overhead vs primitive types (acceptable for V1).
- Requires consistent construction and validation of Money values.
