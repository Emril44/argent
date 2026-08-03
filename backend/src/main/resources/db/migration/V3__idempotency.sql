alter table transactions
add column idempotency_key UUID null,
add constraint uq_idempotency_key unique
(idempotency_key);