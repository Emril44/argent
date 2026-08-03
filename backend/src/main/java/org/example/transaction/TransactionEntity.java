package org.example.transaction;

import jakarta.persistence.*;
import org.example.money.Money;
import org.example.money.MoneyEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    private UUID sourceId;
    private UUID destinationId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name="amount"))
    private MoneyEntity amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public TransactionEntity() {}

    public TransactionEntity(UUID id, UUID idempotencyKey, UUID sourceWalletId, UUID destinationWalletId, Money amount, TransactionType type, TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.sourceId = sourceWalletId;
        this.destinationId = destinationWalletId;
        this.amount = MoneyEntity.mapMoneyToEntity(amount);
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdempotencyKey(){return idempotencyKey;}

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getDestinationId() {
        return destinationId;
    }

    public MoneyEntity getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public static List<Transaction> mapEntitiesToTransactions(List<TransactionEntity> entities) {
        List<Transaction> mappedTransactions = new ArrayList<>();

        for (TransactionEntity entity : entities) {
            Transaction newTransaction = entity.mapEntityToTransaction();
            mappedTransactions.add(newTransaction);
        }

        return mappedTransactions;
    }

    Transaction mapEntityToTransaction() {
        return Transaction.reconstitute(this.getId(), this.getIdempotencyKey(), this.getSourceId(), this.getDestinationId(), this.getAmount().mapEntityToMoney(), this.getType(), this.getStatus(), this.getCreatedAt(), this.getProcessedAt());
    }
}
