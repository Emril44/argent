# ADR-011: CI Gate CVSS Threshold

Date: 2026-09-15
Status: Accepted

## Context
Argent V0.2 features thorough consideration for system security, and among this version's goals is to minimize possible vulnerabilities in the source code.
This includes SAST (as described in ADR-010), but another important step is verifying code **dependencies**, as vulnerabilities there can also be exploited.

## Decision
Argent's CI pipeline will utilize **a Common Vulnerability Scoring System (CVSS) gate**.

- CVSS ≥ 7.0 (High and above) blocks merge
- Any CVE appearing in CISA's Known Exploited Vulnerabilities (KEV) catalog
  blocks merge regardless of CVSS score
- SonarCloud Quality Gate must pass
- Findings with no available fix may be suppressed with documented justification
  in `dependency-check-suppressions.xml`

## Alternatives Considered
- **CVSS ≥ 9.0**

Threshold only blocks critical vulnerabilities. Deemed too unsafe.

- **CVSS ≥ 4.0**

Threshold blocks vulnerabilities of medium and higher severity. Deemed restrictive.

- **EPSS (Exploit Prediction Scoring System)**

- More nuanced than CVSS but not natively supported by OWASP Dependency
  Check. Would require Grype or custom scripting. Deferred to a future milestone.

## Consequences
### Positive
- CVSS + KEV combination gates on both theoretical severity and confirmed
  real-world exploitation, reducing alert fatigue from high-CVSS/low-risk findings
- KEV integration is supported natively by OWASP Dependency Check - minimal
  additional implementation cost

### Negative
- CVSS ≥ 7.0 may still flag transitive dependencies with no reachable exploit
  path in Argent's attack surface - suppression file handles these case by case