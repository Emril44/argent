![][image1]

# ARGENT

## Transaction Integrity Simulator

## **15th February 2026**

## Author: Maksym Khomenko

**Version 0.1**

# **OVERVIEW**

Argent is a backend-focused transaction integrity simulator designed to model core principles of financial systems. This includes:

- Monetary precision  
- Atomic transfers  
- Concurrency control  
- Idempotency  
- Auditability  
- Event-driven notifications

**Argent is not intended to replicate a full brokerage or banking system.** Its purpose is to explore architectural and systemic correctness in a controlled, single-dev environment.

# **GOALS**

## **Primary Goals**

1. Model financial transactions using deterministic monetary arithmetic  
2. Guarantee balance integrity under concurrent access  
3. Prevent double-spend scenarios  
4. Implement idempotent transfer requests  
5. Maintain immutable transaction history  
6. Simulate production-like architecture boundaries

## **Secondary Goals**

1. Introduce event-driven notification flow  
2. Provide a minimal dashboard to visualise transactions  
3. Document architectural decisions in ADR format

## **Non-Goals (Scope Guardrails)**

1. Real stock trading  
2. Integration with external payment providers  
3. Distributed microservices deployment  
4. High-frequency trading simulation  
5. Real financial compliance implementation

# **CORE DOMAIN CONCEPTS**

## **User**

Represents an authenticated identity within the system.

Attributes:

- id  
- fullName  
- Email (unique)  
- passwordHash  
- Status (ACTIVE / UNVERIFIED / LOCKED)  
- createdAt

Notes:

- A User may own multiple Wallets  
- Authentication and session management are tied to User  
- Financial state is not stored directly on User

## **Wallet**

Represents a user-controlled financial container.

Attributes:

- id   
- owner  
- balance (Money)  
- status (ACTIVE / FROZEN)  
- createdAt

Invariants:

- Balance is **never** negative (unless overdraft explicitly supported, not in V1)  
- Frozen wallets cannot initiate transactions  
- Transfers to frozen wallets are allowed; frozen status only blocks outgoing operations  
- A User may only perform operations on Wallets they own

## **Transaction**

Represents a financial state transition.

Attributes

- id (based on SecureRandom)  
- sourceWalletId (nullable for DEPOSIT)  
- destinationWalletId (nullable for WITHDRAW)  
- Amount (Money)  
- type (DEPOSIT, WITHDRAW, TRANSFER)  
- Status (PENDING / SUCCESS / FAILED)  
- createdAt  
- processedAt

**Note:** Deposits and withdrawals are modeled as one-sided transactions against an implicit SYSTEM boundary in V1.

Invariants:

- Transactions are **immutable** after SUCCESS  
- Each transaction must be idempotent  
- Transfer must debit and credit atomically  
- For TRANSFER operations, the total system balance across involved wallets must remain conserved (excluding DEPOSIT and WITHDRAW).

## **Money**

Encapsulated value object:

- BigDecimal amount  
- Fixed scale (2 digits after decimal point)
- Explicit rounding mode (2 digits after decimal point)  
- No raw manipulation outside Money class

# **SYSTEM INVARIANTS**

- Wallet balance is never negative.  
- Successful transactions are immutable.  
- Transfers are atomic.  
- Idempotency guarantees at-most-once effect per key.  
- Frozen wallets cannot initiate balance-changing operations.  
- Users cannot operate on wallets they do not own.

# **ARCHITECTURAL PRINCIPLES**

## **Layered Structure**

Argent will follow a **modular monolith architecture**:

- API layer  
- Application layer  
- Domain layer  
- Infrastructure layer

Domain logic will not depend on:

- HTTP  
- Email  
- Database frameworks

A modular monolith was chosen over a distributed microservice architecture to prioritise domain integrity and development clarity.

Given the single-developer scope and lack of horizontal scaling requirements, introducing network boundaries would add operational complexity without architectural benefit.

## **Transaction Integrity**

All balance-modifying operations must:

- Execute within a database transaction  
- Respect chosen isolation level  
- Prevent race conditions  
- Guarantee atomic state change

## **Idempotency**

Transfer endpoints must support an Idempotency-Key. Repeated requests with the same key must not produce duplicate state changes (to prevent double-charging).

## **Auditability**

Every transaction must:

- Be permanently recorded  
- Be queryable by account  
- Contain sufficient metadata for traceability

# **CONCURRENCY STRATEGY (Draft)**

To be determined.

Considering:

- Pessimistic locking  
- Optimistic locking  
- Database isolation level adjustments

Locking strategy must guarantee that concurrent transfers cannot result in negative balances or double-spend.

Will formalise decision in ADR-002.

# **EVENT MODEL**

Upon transaction completion:

- Emit TransactionCompletedEvent  
- Trigger notification handler  
- Queue email simulation

This will be implemented in-process for V1.

# **MILESTONES**

## **Milestone 1 \- Domain Modeling**

- Define User, Wallet, Transaction, Money  
- Implement invariants  
- Unit tests

## **Milestone 2 \- Persistence & Atomicity**

- Implement repository layer  
- Ensure atomic transfers  
- Concurrency test suite

## **Milestone 3 \- Idempotency**

- Implement key storage  
- Prevent duplicate transfer

## **Milestone 4 \- Event & Notification Layer**

- Domain events  
- Email simulation

## **Milestone 5 \- Deployment**

- Dockerization  
- Public API  
- README documentation

# **USER STORIES**

1. As a user, I can create an account.  
2. As a user, I can log into my existing account.  
3. As a user, I can deposit funds to one of my wallets.  
4. As a user, I can transfer funds between wallets (mine or another user’s).  
5. As a user, I can view a list of my wallets and their balances.  
6. As a user, I can withdraw funds from my wallets.  
7. As a user, I cannot transfer funds if my wallet is frozen.  
8. As a user, retrying the same transfer does not double-charge me.  
9. As a user, I receive a notification after a successful transaction.

# **USE CASE FLOWS**

## **Create Account**

1. User selects Create Account  
2. System displays registration form (name, email, password)  
3. User submits registration data  
4. System validates inputs (format, password policy), normalises email  
5. System checks uniqueness (email) and enforces via DB constraint  
6. System creates User and associated Wallet (status: UNVERIFIED)  
   1. If status is UNVERIFIED, the system does not issue a session; users must verify email before login  
7. System stores **hashed** password   
8. System emits UserRegistered event  
9. Notification consumer sends welcome/verification email (retry on failure)  
10. System returns success and redirects to “verify email” page

## **Log Into Account**

1. User selects Log In  
2. System displays login form (email, password)  
3. User submits credentials  
4. System validates inputs, normalises email  
5. System attempts authentication  
   1. Search user by email  
   2. Verify password hash  
6. If authentication succeeds:  
   1. System verifies account status (e.g., UNVERIFIED users must verify email before login; FROZEN users may be denied or restricted based on policy)  
   2. System creates a server-side session and returns a Secure, HttpOnly session cookie (SameSite=Lax)  
   3. System records a UserLoggedIn audit event  
   4. User is redirected to dashboard  
7. If authentication fails:  
   1. System returns a generic warning (“Invalid email or password”)  
   2. System increments failed-attempt counter / rate limits  
   3. After 5 failures, temporarily lock account or require cooldown

## **Log Out of Account**

1. User selects Log Out  
2. System invalidates server-side session  
3. System clears session cookie  
4. User is redirected to login page

## **Create Wallet**

1. User selects Create Wallet  
2. System authenticates via session  
3. System validates optional wallet name/label  
4. System creates Wallet (balance 0, status ACTIVE, ownerId \= User.id)  
5. System returns wallet details

Optional:

- Limit the number of wallets per user (max 5?)

## **Deposit Funds (Simulated)**

1. User selects Deposit Funds for a specific Wallet  
2. System authenticates via session and resolves User  
3. System verifies Wallet belongs to authenticated User  
4. System validates amount (positive, within configured limits; scale rules)  
5. System begins DB transaction  
6. System creates DEPOSIT transaction record (PENDING)  
7. System credits Wallet balance by amount (Money rules)  
8. System marks transaction SUCCESS and persists changes  
9. System commits DB transaction  
10. System emits TransactionCompleted event  
11. Notification consumer sends deposit receipt email  
12. System returns success and updated balance

**Important:**

- Deposits are “trusted” simulated credits in V1 (no external verification)  
- Consider daily deposit limit for realism

## **Transfer Funds (Simulated)**

1. User initiates transfer (sourceWalletId, destinationWalletId, amount) with Idempotency-Key  
2. System authenticates via session and resolves User  
3. System verifies source Wallet belongs to User  
4. System validates request (amount positive; destination exists; not same wallet unless allowed)  
5. System checks source Wallet status (must be ACTIVE)  
6. System checks idempotency store for (userId, idempotencyKey)  
   1. If SUCCESS exists, return stored result  
7. System begins DB transaction  
8. System loads source and destination Wallets with concurrency control (locking strategy TBD)  
9. System re-validates invariants under lock:  
   1. Source has sufficient funds  
   2. Source not FROZEN  
10. System creates TRANSFER transaction record (PENDING)  
11. System debits source Wallet and credits destination Wallet atomically  
12. System marks transaction SUCCESS and persists:  
    1. Wallet balances  
    2. Transaction record  
    3. Idempotency record  
13. System commits DB transaction  
14. System emits TransactionCompleted event  
15. Notification consumer sends transfer receipt email  
16. System returns success response

**Important:**

Idempotency should be persisted as part of the same DB transaction, otherwise retries under failure can still double-apply.

## **Withdraw Funds (Simulated)**

1. User selects Withdraw Funds for a specific Wallet  
2. System authenticates via session and resolves User  
3. System verifies Wallet belongs to User  
4. System validates amount (positive; within limits)  
5. System checks Wallet status (must be ACTIVE)  
6. System begins DB transaction  
7. System creates WITHDRAW transaction record (PENDING)  
8. System validates sufficient funds  
9. System debits Wallet balance  
10. System marks transaction SUCCESS and persists  
11. System commits DB transaction  
12. System emits TransactionCompleted event  
13. Notification consumer sends withdrawal receipt email  
14. System returns success and updated balance

**Optional:**

- Add cooldown / velocity checks later

## **Freeze Account**

1. Admin/system triggers Freeze Account for a target wallet  
2. System authenticates admin/system action (for V1, admin key or admin login)  
3. System begins DB transaction  
4. System sets wallet status to FROZEN  
5. System records AccountFrozen audit event (who, when, why)  
6. System commits DB transaction  
7. System emits AccountStatusChanged event  
8. Notification consumer sends freeze notification email  
9. System returns success

## **Unfreeze Account**

1. Admin/system triggers Unfreeze Account  
2. System authenticates admin/system action  
3. System begins DB transaction  
4. System sets wallet status to ACTIVE  
5. System records AccountUnfrozen audit event  
6. System commits DB transaction  
7. System emits AccountStatusChanged event  
8. Notification consumer sends unfreeze notification email  
9. System returns success

## **Get Account Summary**

1. User navigates to dashboard  
2. System authenticates user via session  
3. System retrieves wallet summary  
   1. Current balance  
   2. Status  
   3. Recent transaction list (last 5\)  
4. System returns summary to client

## **List Transactions**

1. User opens transaction page  
2. System authenticates via session  
3. System queries transactions by wallet/user with optional filters  
   1. Date range  
   2. Type  
   3. Status  
4. System returns paginated results

## **Idempotent Transfer Retry**

1. Client retries the same transfer with the same Idempotency-Key  
2. System authenticates user via session  
3. System checks idempotency store for (userId, idempotencyKey)  
4. If found and marked **SUCCESS**:  
   1. System returns the previously stored response (txId, balances) **without** modifying state  
5. If found and marked IN\_PROGRESS:  
   1. System returns 409 Conflict indicating the request is currently processing and the client should retry with the same Idempotency-Key.  
6. If not found:  
   1. System proceeds with Transfer Funds flow

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnAAAAAHCAYAAACIq3DzAAAAQUlEQVR4Xu3WMQ0AIADAMFziCFGYgx8FLOnRZwo25l4HAICO8QYAAP5m4AAAYgwcAECMgQMAiDFwAAAxBg4AIOYClIUh9UOLBN8AAAAASUVORK5CYII=>