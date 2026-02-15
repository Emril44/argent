# ADR-001: Authentication for V1 — Server-Side Sessions (Secure Cookies)

Date: 2026-02-15  
Status: Accepted  
Project: Argent

## Context

Argent V1 is a single-developer, modular-monolith system intended to model financial-system integrity (precision, atomicity, auditability, idempotency). Authentication must be secure in a browser environment and support fast revocation.

Previous projects used JWT stored in `localStorage`, which is not acceptable for Argent due to token exposure risk under XSS.

## Decision

Argent V1 will use **server-side session authentication** with a **Secure, HttpOnly cookie** carrying a session identifier.

- On successful login, the server creates a session record (DB-backed for V1).
- The client receives a cookie containing a session ID.
- Each request is authenticated by validating the session ID and resolving a `UserContext` / `Principal`.

Cookie requirements:
- `HttpOnly` (not accessible to JavaScript)
- `Secure` (HTTPS only)
- `SameSite=Lax` (default; adjust if needed)

Session requirements:
- Server-side expiration (idle + absolute timeout)
- Logout invalidates the session server-side
- Basic rate limiting on login attempts
- Generic error messaging for failed login (“Invalid email or password”)

## Rationale

- **Browser-safe by default**: avoids `localStorage` token theft via XSS.
- **Easy revocation**: deleting a session immediately removes access.
- **Lower complexity** for V1 compared to access/refresh token lifecycles.
- Fits modular monolith scope: avoids introducing distributed token revocation concerns.

## Consequences

### Positive
- Simple and robust authentication model for a solo project.
- Clear security posture suitable for a fintech-themed system.
- Authentication mechanism is easy to test (session creation, expiry, invalidation).

### Negative
- Requires server-side session storage and cleanup.
- If cookies are used for auth, state-changing requests may require CSRF considerations depending on client architecture.

## Alternatives Considered

1. **JWT stored in localStorage**  
   Rejected due to XSS token exfiltration risk and weak revocation.

2. **JWT access token + refresh token stored in HttpOnly cookies**  
   Viable, but deferred to a future milestone due to additional complexity (refresh rotation, reuse detection, token storage).

3. **OAuth2/OIDC**  
   Rejected for V1 to avoid integration scope creep; not core to Argent’s transaction integrity goals.

## Notes / Migration Path (V2+)

Argent may migrate to:
- **JWT access tokens + refresh tokens in HttpOnly cookies**, with refresh token rotation and server-side storage (hashed refresh tokens).

To keep migration straightforward, core business logic will depend only on an abstract `UserContext`/`Principal`, not on session-specific APIs.
