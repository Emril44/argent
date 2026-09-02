package org.example.wallet;

import org.example.money.Money;

import java.time.LocalDateTime;
import java.util.UUID;

public class WalletResponse {
    private final UUID id;
    private final UUID ownerId;
    private Money balance;
    private WalletStatus status;
    private final LocalDateTime createdAt;

    public WalletResponse(UUID id, UUID ownerId, Money balance, WalletStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Money getBalance() {
        return balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
