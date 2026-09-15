# ADR-00X: Title

Date: 2026-09-06
Status: Accepted

## Context
Argent V0.2 features thorough consideration for system security, and among this version's goals is to minimize possible vulnerabilities in the source code. To achieve this, the project requires Static Application Security Testing (SAST).

This follows the philosophy of "shifting left," detecting known vulnerability patterns and code quality issues before merge.

## Decision
Argent V0.2 will use **SonarQube Cloud** to perform SAST.

- Public dashboard available for review
- Covers code smells and coverage beyond pure security scanning

## Alternatives Considered
1. **SpotBugs + Find Security Bugs**
    Maven-native and without external services, but does not feature a dashboard

2. **Semgrep**
    Language-agnostic and follows modern rules, but features less Java-specific depth

## Consequences
### Positive
- Widely adopted in professional Java projects; including SonarQube Cloud demonstrates pipeline maturity beyond a basic `build-and-test`

### Negative
- Requires an external service account and public repo visibility for the free tier