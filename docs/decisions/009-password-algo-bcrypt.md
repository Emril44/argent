# ADR-009: Password Hashing Algorithm - BCrypt

Date: 2026-09-06
Status: Accepted

## Context
Argent V0.2 features consideration for user / system security, thus absolutely requires safe handling of sensitive data.

Among the needed security measures is password hashing, to not store them in plaintext while making sure the hashing process is secure and resistant to attacks.

## Decision
Argent V0.2 will implement **BCrypt** as its hashing algorithm via Spring Security's `BCryptPasswordEncoder`.

- The cost factor will be set to **12**
- BCrypt generates and embeds a unique salt per hash automatically

## Alternatives Considered
1. **BCrypt (cost factor 14)**
    Rejected due to high latency (~1 second per hash).

2. **Argon2id**
    Possibly implemented later, as Argon2id is the modern recommendation **(winner of the Password Hashing Competition)**. Worth revisiting if security posture hardens in future.

3. **SHA-256**
    Rejected due to the speed (and thus, vulnerability to brute-force attacks).


## Consequences
### Positive
- Industry-standard password hashing
- Salting negates rainbow table attacks

### Negative
- Latency cost (~250-300ms at cost factor 12)
