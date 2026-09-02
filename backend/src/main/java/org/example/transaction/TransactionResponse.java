package org.example.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.money.Money;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private final UUID id;
    private UUID idempotencyKey;
    private final UUID sourceWalletId;
    private final UUID destinationWalletId;
    private final Money amount;
    private final TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public TransactionResponse(UUID id, UUID idempotencyKey, UUID sourceWalletId, UUID destinationWalletId, Money amount, TransactionType type, TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.sourceWalletId = sourceWalletId;
        this.destinationWalletId = destinationWalletId;
        this.amount = amount;
        this.transactionType = type;
        this.transactionStatus = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getSourceWalletId() {
        return sourceWalletId;
    }

    public UUID getDestinationWalletId() {
        return destinationWalletId;
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
