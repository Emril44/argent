package org.example.transaction;

import org.example.money.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionTransferRequest {
    private UUID sourceId;
    private UUID destinationId;
    private UUID idempotencyKey;
    private BigDecimal amount;

    public TransactionTransferRequest(UUID sourceId, UUID destinationId, UUID idempotencyKey, BigDecimal amount) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getDestinationId() {
        return destinationId;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
