package org.example.wallet;

import jakarta.persistence.*;
import org.example.money.Money;
import org.example.money.MoneyEntity;
import org.example.user.User;
import org.example.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class WalletEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @Embedded
    private MoneyEntity balance;

    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WalletEntity(UserEntity owner) {
        this.owner = owner;
        this.balance = new MoneyEntity(new BigDecimal("0.00"));
        this.status = WalletStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public WalletEntity(UUID id, UserEntity owner, Money balance, WalletStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.owner = owner;
        this.balance = MoneyEntity.mapMoneyToEntity(balance);
        this.status = status;
        this.createdAt = createdAt;
    }

    public WalletEntity() {}

    public UUID getId() {
        return id;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public MoneyEntity getBalance() {
        return balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static List<Wallet> mapEntitiesToWallets(List<WalletEntity> userWallets, User owner) {
        List<Wallet> mappedWallets = new ArrayList<>();

        for (WalletEntity userWallet : userWallets) {
            Wallet newWallet = userWallet.mapEntityToWallet(owner);
            mappedWallets.add(newWallet);
        }

        return mappedWallets;
    }

    public Wallet mapEntityToWallet(User owner) {
        return Wallet.reconstitute(this.getId(), owner, this.getBalance().mapEntityToMoney(), this.getStatus(), this.getCreatedAt());
    }
}
