package org.example.transaction;

import org.example.money.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionRequest {
    private UUID walletId;
    private UUID idempotencyKey;
    private BigDecimal amount;

    public TransactionRequest(UUID walletId, UUID idempotencyKey, BigDecimal amount) {
        this.walletId = walletId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
