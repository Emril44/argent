package org.example.transaction;

import org.example.exceptions.IllegalStatusTransitionException;
import org.example.money.Money;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class Transaction {
    private final UUID id;
    private final UUID sourceWalletId;
    private final UUID destinationWalletId;
    private final Money amount;
    private final TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private Transaction(UUID id, UUID sourceWalletId, UUID destinationWalletId, Money amount, TransactionType type, TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.sourceWalletId = sourceWalletId;
        this.destinationWalletId = destinationWalletId;
        this.amount = amount;
        this.transactionType = type;
        this.transactionStatus = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static Transaction deposit(UUID destinationWalletId, Money amount) {
        UUID id = UUID.randomUUID();
        TransactionType type = TransactionType.DEPOSIT;
        TransactionStatus status = TransactionStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        return new Transaction(id, null, destinationWalletId, amount, type, status, createdAt, null);
    }

    public static Transaction withdraw(UUID sourceWalletId, Money amount) {
        UUID id = UUID.randomUUID();
        TransactionType type = TransactionType.WITHDRAWAL;
        TransactionStatus status = TransactionStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        return new Transaction(id, sourceWalletId, null, amount, type, status, createdAt, null);
    }

    public static Transaction transfer(UUID sourceWalletId, UUID destinationWalletId, Money amount) {
        if(sourceWalletId.equals(destinationWalletId))
            throw new IllegalArgumentException("Cannot transfer to the same wallet!");

        UUID id = UUID.randomUUID();
        TransactionType type = TransactionType.TRANSFER;
        TransactionStatus status = TransactionStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        return new Transaction(id, sourceWalletId, destinationWalletId, amount, type, status, createdAt, null);
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
}
