package org.example.transaction;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionCompletedEvent {
    private final UUID userId;
    private final UUID transactionId;
    private final LocalDateTime processedAt;
    private final TransactionStatus status;

    public TransactionCompletedEvent(UUID userId, UUID transactionId, LocalDateTime processedAt, TransactionStatus status) {
        this.userId = userId;
        this.transactionId = transactionId;
        this.processedAt = processedAt;
        this.status = status;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}
