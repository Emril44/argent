package org.example.wallet;

import org.example.exceptions.IllegalStatusTransitionException;
import org.example.money.Money;
import org.example.user.User;
import org.example.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private Wallet(UUID id, User owner, Money balance, WalletStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
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
        if(this.getStatus().equals(WalletStatus.CLOSED) || this.getStatus().equals(WalletStatus.FROZEN))
            throw new IllegalStatusTransitionException("Closed/Frozen Wallets cannot be frozen!");

        this.setStatus(WalletStatus.FROZEN);
    }

    public void unfreeze() {
        if(this.getStatus().equals(WalletStatus.CLOSED) || this.getStatus().equals(WalletStatus.ACTIVE))
            throw new IllegalStatusTransitionException("Active/Closed Wallets cannot be unfrozen!");

        this.setStatus(WalletStatus.ACTIVE);
    }

    public void close() {
        if(!this.getBalance().isZero())
            throw new IllegalStatusTransitionException("Cannot close Wallet with existing balance!");

        if(this.getStatus().equals(WalletStatus.FROZEN))
            throw new IllegalStatusTransitionException("Frozen accounts cannot be closed!");

        this.setStatus(WalletStatus.CLOSED);
    }

    public void deposit(Money newMoney) {
        if(this.getStatus().equals(WalletStatus.FROZEN) || this.getStatus().equals(WalletStatus.CLOSED))
            throw new IllegalStatusTransitionException("Cannot deposit funds!");
        Money currentMoney = this.getBalance().add(newMoney);
        this.setBalance(currentMoney);
    }

    public void debit(Money takenMoney) {
        if(this.getStatus().equals(WalletStatus.FROZEN) || this.getStatus().equals(WalletStatus.CLOSED))
            throw new IllegalStatusTransitionException("Cannot debit funds!");
        Money currentMoney = this.getBalance().subtract(takenMoney);
        this.setBalance(currentMoney);
    }

    static Wallet reconstitute(UUID id, User owner, Money balance, WalletStatus status, LocalDateTime createdAt) {
        return new Wallet(id, owner, balance, status, createdAt);
    }

    public static List<WalletEntity> mapWalletsToEntities(List<Wallet> userWallets, UserEntity userEntity) {
        List<WalletEntity> mappedEntities = new ArrayList<>();

        for (Wallet wallet : userWallets) {
            WalletEntity newEntity = wallet.mapWalletToEntity(userEntity);
            mappedEntities.add(newEntity);
        }

        return mappedEntities;
    }

    public WalletEntity mapWalletToEntity(UserEntity userEntity) {
        return new WalletEntity(this.getId(), userEntity, this.getBalance(), this.getStatus(), this.getCreatedAt());
    }
}
