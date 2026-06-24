package org.example.user;

import jakarta.persistence.*;
import org.example.wallet.Wallet;
import org.example.wallet.WalletEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.example.wallet.Wallet.mapWalletsToEntities;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "full_name")
    private String name;

    private String email;
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany
    @JoinColumn(name = "owner_id")
    private List<WalletEntity> userWallets;

    public UserEntity(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = UserStatus.UNVERIFIED;
        this.createdAt = LocalDateTime.now();
        this.userWallets = new ArrayList<>();
    }

    public UserEntity(UUID uuid, String name, String email, String passwordHash, UserStatus status, LocalDateTime createdAt, List<Wallet> wallets) {
        this.id = uuid;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.createdAt = createdAt;
        this.userWallets = mapWalletsToEntities(wallets, this);
    }

    public UserEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<WalletEntity> getUserWallets() {
        return userWallets;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    // Wallets hydrated separately after User shell is created to break circular dependency
    public User mapEntityToUser() {
        User newUser = User.reconstitute(this.getId(), this.getName(), this.getEmail(), this.getPasswordHash(), this.getStatus(), this.getCreatedAt());
        List<Wallet> wallets = WalletEntity.mapEntitiesToWallets(userWallets, newUser);
        newUser.hydrateWallets(wallets);

        return newUser;
    }
}
