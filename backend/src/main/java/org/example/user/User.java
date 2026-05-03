package org.example.user;

import org.example.exceptions.IllegalStatusTransitionException;
import org.example.exceptions.MaxWalletsExceededException;
import org.example.wallet.Wallet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User {
    private static final int MAX_WALLETS = 5;
    private final UUID id;
    private final String name;
    private final String email;
    private String passwordHash;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private List<Wallet> userWallets;

    public User(String name, String email, String passwordHash) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = UserStatus.UNVERIFIED;
        this.createdAt = LocalDateTime.now();
        this.userWallets = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    private void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Wallet> getUserWallets() {
        return Collections.unmodifiableList(userWallets);
    }

    public Wallet openWallet() {
        if(this.getUserWallets().size() >= MAX_WALLETS) {
            throw new MaxWalletsExceededException("Wallet limit reached.");
        }
        Wallet newWallet = new Wallet(this);
        userWallets.add(newWallet);

        return newWallet;
    }

    public void verify() {
        if(this.getStatus().equals(UserStatus.ACTIVE))
            throw new IllegalStatusTransitionException("Active users are already verified!");

        this.setStatus(UserStatus.ACTIVE);
    }

    public void lock() {
        if(this.getStatus().equals(UserStatus.LOCKED))
            throw new IllegalStatusTransitionException("User is already locked!");

        this.setStatus(UserStatus.LOCKED);
    }
}
