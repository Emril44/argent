package org.example.transaction;

import org.example.exceptions.IllegalStatusTransitionException;
import org.example.money.Money;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class Transaction {
    private final UUID id;
    private UUID idempotencyKey;
    private final UUID sourceWalletId;
    private final UUID destinationWalletId;
    private final Money amount;
    private final TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private Transaction(UUID id, UUID idempotencyKey, UUID sourceWalletId, UUID destinationWalletId, Money amount, TransactionType type, TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
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

    public static Transaction deposit(UUID destinationWalletId, UUID idempotencyKey, Money amount) {
        UUID id = UUID.randomUUID();
        TransactionType type = TransactionType.DEPOSIT;
        TransactionStatus status = TransactionStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        return new Transaction(id, idempotencyKey, null, destinationWalletId, amount, type, status, createdAt, null);
    }

    public static Transaction withdraw(UUID sourceWalletId, UUID idempotencyKey, Money amount) {
        UUID id = UUID.randomUUID();
        TransactionType type = TransactionType.WITHDRAWAL;
        TransactionStatus status = TransactionStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        return new Transaction(id, idempotencyKey, sourceWalletId, null, amount, type, status, createdAt, null);
    }

    public static Transaction transfer(UUID sourceWalletId, UUID destinationWalletId, UUID idempotencyKey, Money amount) {
        if(sourceWalletId.equals(destinationWalletId))
            throw new IllegalArgumentException("Cannot transfer to the same wallet!");

        UUID id = UUID.randomUUID();
        TransactionType type = TransactionType.TRANSFER;
        TransactionStatus status = TransactionStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        return new Transaction(id, idempotencyKey, sourceWalletId, destinationWalletId, amount, type, status, createdAt, null);
    }

    static Transaction reconstitute(UUID id, UUID idempotencyKey, UUID sourceId, UUID destinationId, Money amount, TransactionType type, TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
        return new Transaction(id, idempotencyKey, sourceId, destinationId, amount, type, status, createdAt, processedAt);
    }

    public void complete() {
        if(this.isTerminal())
            throw new IllegalStatusTransitionException("Terminal status reached!");

        this.processedAt = LocalDateTime.now();
        this.setTransactionStatus(TransactionStatus.SUCCESS);
    }

    public void fail() {
        if(this.isTerminal())
            throw new IllegalStatusTransitionException("Terminal status reached!");

        this.processedAt = LocalDateTime.now();
        this.setTransactionStatus(TransactionStatus.FAILED);
    }

    private boolean isTerminal() {
        return this.getTransactionStatus().equals(TransactionStatus.SUCCESS) || this.getTransactionStatus().equals(TransactionStatus.FAILED);
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdempotencyKey() {return idempotencyKey;}

    public Optional<UUID> getSourceWalletId() {
        return Optional.ofNullable(sourceWalletId);
    }

    public Optional<UUID> getDestinationWalletId() {
        return Optional.ofNullable(destinationWalletId);
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

    public Optional<LocalDateTime> getProcessedAt() {
        return Optional.ofNullable(processedAt);
    }

    private void setTransactionStatus(TransactionStatus status) {
        this.transactionStatus = status;
    }

    public TransactionEntity mapTransactionToEntity() {
        return new TransactionEntity(this.getId(), this.getIdempotencyKey(), this.getSourceWalletId().orElse(null), this.getDestinationWalletId().orElse(null), this.getAmount(), this.getTransactionType(), this.getTransactionStatus(), this.getCreatedAt(), this.getProcessedAt().orElse(null));
    }
}
