# ARGENT

## Transaction Integrity Simulator

## **5th September 2026**

## Author: Maksym Khomenko

**Version 0.2**

# **OVERVIEW**
Argent is a backend-focused transaction integrity simulator designed to model core principles of financial systems.
Version 0.2 builds on the existing core functionality by introducing:
- Authentication and session-based authorisation (Spring Security + BCrypt)
- OWASP Top 10 (2025) security controls mapped and addressed across the stack 
- Input validation and hardened error handling 
- CI/CD pipeline with integrated security gates (DevSecOps)

# **GOALS**

## **Primary Goals**

1. All endpoints (except `/register`, `/login`) require an authenticated session - no client-supplied identity 
2. OWASP Top 10 (2025) coverage fully documented; each item either controlled or explicitly risk-accepted with justification 
3. CI pipeline runs on every push: build → tests → SAST → dependency scan → container image scan; critical findings block merge 
4. Passwords stored as BCrypt hashes, never plaintext 
5. Error responses expose no internal implementation details (exception types, stack traces, field names)
6. All request bodies validated with Bean Validation; invalid input returns structured 400 before hitting business logic

## **Out of Scope**

1. Cloud infrastructure (VPC, RDS, ECS, ALB, Secrets Manager) - V0.3 
2. Observability stack (Prometheus, Grafana, centralised logging) - V0.4 
3. React frontend - V1.0 
4. TLS termination - cloud concern, deferred to V0.3 
5. Session store persistence across restarts (in-memory sessions acceptable for V0.2)
6. OAuth2 / SSO / third-party identity providers

## Threat Model

| Threat                     | Argent concern                                   | Severity | Control / Acceptance                                       |
|----------------------------|--------------------------------------------------|----------|------------------------------------------------------------|
| **Spoofing**               | Impersonating another user                       | High     | Session auth; userId from server-side session only         |
| **Tampering**              | Manipulating request amounts or IDs              | Medium   | Input validation; JPQL parameterisation; idempotency keys  |
| **Repudiation**            | Denying a transaction occurred                   | Medium   | Transaction event log; immutable transaction records in DB |
| **Information Disclosure** | Accessing another user's wallet/transaction data | High     | Ownership check on every resource access                   |
| **Denial of Service**      | Flooding endpoints                               | Low      | Accepted; deferred to V0.3 API gateway                     |
| **Elevation of Privilege** | No role hierarchy - flat auth model              | Low      | Accepted; no admin surface exists by design                |


## Security Controls
OWASP Top 10 (2025) mapped to Argent:

| Item                                          | Argent exposure                                                                                                     | Control                                                                             |
|-----------------------------------------------|---------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **A01** Broken Access Control                 | **Critical** - `userId` from request header is trivially spoofable, IDOR on every wallet/tx endpoint                | Session auth; `userId` sourced from `SecurityContextHolder`, never from client      |
| **A02** Security Misconfiguration             | **Medium** - Spring Boot actuator likely exposed by default, no CORS policy, no error message sanitisation          | Restrict actuator endpoints, configure CORS, global exception handler               |
| **A03** Software Supply Chain Failures        | **Unknown** - no dependency scanning in place                                                                       | OWASP Dependency Check in CI, pin GitHub Actions to SHA not tag, Dependabot         |
| **A04** Cryptographic Failures                | **High** - password field accepted as plain string in V0.1, no hashing                                              | `BCrypt` (cost factor ~12), HTTPS enforced in deployed env (V0.3)                   |
| **A05** Injection                             | **Low** - JPQL parameterised throughout, but no input validation on request bodies                                  | Bean Validation (`@Valid`, `@NotBlank`, size constraints), audit any native queries |
| **A06** Insecure Design                       | **Medium** - no rate limiting, no account lockout after repeated failed logins                                      | Rate limiting deferred to V0.3; API gateway is the appropriate layer                |
| **A07** Auth Failures                         | **Critical** - no auth in V0.1                                                                                      | Spring Security session auth, `HttpOnly`+`SameSite` cookies                         |
| **A08** Software/Data Integrity Failures      | **Low** - no CI pipeline, no artifact verification                                                                  | Pipeline gates, verify artifact integrity in CI                                     |
| **A09** Security Logging & Alerting Failures  | **Partial** - transaction events logged, but no failed auth logging                                                 | Audit log for failed auth attempts, ensure no credentials/PII in log output         |
| **A10** Mishandling of Exceptional Conditions | **Medium** - Spring Boot default error responses likely leak internal details (exception class names, stack traces) | Global `@ControllerAdvice` exception handler returning sanitised error DTOs only    |


## Spring Security Design

### Authentication Flow
- `POST /auth/login` - accepts email + password, creates session, returns 200 + session cookie. On failure: 401 with no detail (not to distinguish wrong password from unknown email)
- `POST /auth/logout` - requires authentication; invalidates session 
- `POST /users/register` - public 
- All other endpoints require authenticated session

### Endpoint Security

- Two public endpoints: `/auth/login`, `/users/register` 
- Everything else: authenticated 
- 401 returned for unauthenticated requests, 403 for authenticated-but-unauthorised (Spring Security default returns 403 for both - explicitly override)

### Session Management

- In-memory session store (single instance, acceptable for V0.2)
- Idle timeout: 30 minutes 
- Session fixation protection: enabled by default in Spring Security (new session ID issued on login)
- Redis-backed session store deferred to V0.3

### CSRF

- Spring Security's CSRF token mechanism disabled - designed for form-based HTML, not REST 
- Mitigation: `SameSite=Lax` on session cookie per ADR-001, preventing cross-origin requests from carrying the cookie

### UserDetails

- Custom `ArgentUserDetails` implements Spring Security's `UserDetails` 
- Wraps `UserEntity`; provides email as username, `BCrypt` password hash 
- `isAccountNonLocked`, `isAccountNonExpired`, `isCredentialsNonExpired`, `isEnabled` all return true for V0.2 - lockout deferred 
- `UserDetailsService` implementation loads by email from `UserRepository`

### Password Encoding

- `BCrypt`, cost factor 12 (~250-300ms on commodity hardware)
- Hashing on registration; comparison on login via Spring Security's `AuthenticationManager` 
- Plain-text password never persisted

### Controller Migration

- `@RequestHeader UUID userId` removed from all controllers 
- Replaced with `@AuthenticationPrincipal ArgentUserDetails` — `userId` sourced from server-side session only 
- Affects: `WalletController`, `TransactionController`

## CI/CD Pipeline Design
**Trigger:**
- Pipeline runs on push to `main` and on pull requests targeting `main`

**Jobs:**
`build-and-test`
- Maven build + full Testcontainers integration suite
- `ubuntu-latest` runner (Docker pre-installed)
- Verify `docker-java.properties` API version fix is not needed in CI environment

`sast`
- SonarQube Cloud analysis
- Runs after build-and-test passes
- Public dashboard URL in README

`dependency-check`
- OWASP Dependency Check maven plugin
- NVD database cached between runs via GitHub Actions cache
- Runs in parallel with `docker-build-and-scan`

`docker-build-and-scan`
- Build Docker image
- Trivy scan against built image
- Runs in parallel with `dependency-check`

**Gate Policy (ADR-009)**
- CVSS ≥ 7.0 blocks merge
- Findings with no available fix may be suppressed with documented justification in `dependency-check-suppress.xml`
- SonarQube Cloud Quality Gate must pass

```
push/PR
  └── build-and-test          (Maven + Testcontainers)
        ├── sast                    (SonarQube Cloud)
        ├── dependency-check        (OWASP Dependency Check)
        └── docker-build-and-scan   (docker build → Trivy)
  ```

## Milestone Breakdown
### Auth & Authorisation
- [ ] Implement `ArgentUserDetails` and `UserDetailsService`
- [ ] Configure Spring Security filter chain (public vs. protected endpoints)
- [ ] `POST /auth/login` and `POST /auth/logout` endpoints
- [ ] BCrypt password hashing on registration
- [ ] Remove `@RequestHeader UUID userId` from `WalletController`, `TransactionController`
- [ ] Replace with `@AuthenticationPrincipal ArgentUserDetails`

### Input Hardening & Error Handling
- [ ] Bean Validation on all request DTOs (`@Valid`, `@NotBlank`, size constraints)
- [ ] Global `@ControllerAdvice` exception handler - sanitised error DTOs only
- [ ] Restrict Spring Boot actuator endpoints
- [ ] Configure CORS policy
- [ ] Audit failed auth attempt logging

### CI/CD Pipeline
- [ ] GitHub Actions workflow: build-and-test job (Maven + Testcontainers)
- [ ] SonarQube Cloud integration
- [ ] OWASP Dependency Check job with NVD cache
- [ ] Docker build + Trivy scan job
- [ ] Gate policy: CVSS ≥ 7.0 blocks merge, SonarQube Cloud Quality Gate must pass
- [ ] Pin all GitHub Actions to SHA
- [ ] Enable Dependabot