package org.example.wallet;

import org.example.exceptions.IllegalWalletStatusException;
import org.example.exceptions.InsufficientFundsException;
import org.example.money.Money;
import org.example.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Wallet {
    private final UUID id;
    private final User owner;
    private Money balance;
    private WalletStatus status;
    private final LocalDateTime createdAt;

    public Wallet(User owner) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.balance = new Money(new BigDecimal("0.00"));
        this.status = WalletStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public Money getBalance() {
        return balance;
    }

    private void setBalance(Money balance) {
        this.balance = balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    private void setStatus(WalletStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void freeze() {
        if(this.getStatus().equals(WalletStatus.CLOSED))
            throw new IllegalWalletStatusException("Closed Wallets cannot be frozen!");

        this.setStatus(WalletStatus.FROZEN);
    }

    public void unfreeze() {
        if(this.getStatus().equals(WalletStatus.CLOSED))
            throw new IllegalWalletStatusException("Closed Wallets cannot be unfrozen!");

        this.setStatus(WalletStatus.ACTIVE);
    }

    public void close() {
        if(!this.getBalance().isZero())
            throw new IllegalWalletStatusException("Cannot close Wallet with existing balance!");

        if(this.getStatus().equals(WalletStatus.FROZEN))
            throw new IllegalWalletStatusException("Frozen accounts cannot be closed!");

        this.setStatus(WalletStatus.CLOSED);
    }

    public void deposit(Money newMoney) {
        if(this.getStatus().equals(WalletStatus.FROZEN) || this.getStatus().equals(WalletStatus.CLOSED))
            throw new IllegalWalletStatusException("Cannot deposit funds!");
        Money currentMoney = this.getBalance().add(newMoney);
        this.setBalance(currentMoney);
    }

    public void debit(Money takenMoney) {
        if(this.getStatus().equals(WalletStatus.FROZEN) || this.getStatus().equals(WalletStatus.CLOSED))
            throw new IllegalWalletStatusException("Cannot debit funds!");
        Money currentMoney = this.getBalance().subtract(takenMoney);
        this.setBalance(currentMoney);
    }
}
