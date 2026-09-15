# ADR-00X: Title

Date: 2026-09-15
Status: Accepted

## Context
Spring Security enables CSRF token validation by default - 
designed for server-rendered HTML where tokens are embedded in forms. 

Argent is a REST API with session cookie authentication,
where CSRF is a real concern (cookies are sent automatically 
by browsers on cross-origin requests). 
Separate token management provides possibly unnecessary overhead.

## Decision
Argent will disable Spring Security's CSRF token mechanism. 
Mitigation is `SameSite=Lax` on the session cookie per ADR-001.

The mechanism: a cross-site POST from evil.com is not a top-level navigation, 
so the browser does not attach the session cookie - 
the request arrives unauthenticated and is rejected with 401 before reaching any business logic.

## Alternatives Considered
- Spring Security CSRF tokens (synchronizer token pattern)

Correct for form-based apps; 
adds token generation, storage, and validation overhead 
that serves no purpose in a stateless REST client context.

- `SameSite=Strict`

Stronger than Lax but breaks legitimate cross-site navigations 
(e.g. clicking a link to Argent from another site would drop the cookie). 
Lax is the appropriate balance.

- Custom CSRF header (X-Requested-With)

Browser CORS policy blocks custom headers on cross-origin requests, 
providing similar protection; 
adds client implementation burden with no advantage over `SameSite=Lax` here.

## Consequences
### Positive
- Browser-enforced
- Zero server-side token overhead
- No client implementation required

### Negative
- Relies on browser `SameSite` support — effectively universal in modern browsers, but worth noting as an assumption
- Non-browser clients (curl, Postman) are unaffected since CSRF is a browser-specific attack vector