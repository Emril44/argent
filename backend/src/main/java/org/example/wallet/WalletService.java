package org.example.wallet;

import org.example.exceptions.InsufficientFundsException;
import org.example.money.Money;
import org.example.user.UserEntity;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public boolean verifyOwnership(UUID walletId, UUID authenticatedUserId) {
        WalletEntity loadedEntity = this.loadEntity(walletId);
        return loadedEntity.getOwner().getId().equals(authenticatedUserId);
    }

    public void credit(UUID destinationWalletId, Money amount) {
        WalletEntity loadedEntity = this.loadEntity(destinationWalletId);
        UserEntity owner = userRepository.findById(loadedEntity.getOwner().getId()).orElseThrow();
        Wallet wallet = loadedEntity.mapEntityToWallet(owner.mapEntityToUser());

        wallet.deposit(amount);

        loadedEntity = wallet.mapWalletToEntity(owner);
        walletRepository.save(loadedEntity);
    }

    public boolean debit(UUID sourceWalletId, Money amount) {
        WalletEntity loadedEntity = this.loadEntity(sourceWalletId);
        UserEntity owner = userRepository.findById(loadedEntity.getOwner().getId()).orElseThrow();
        Wallet wallet = loadedEntity.mapEntityToWallet(owner.mapEntityToUser());

        try {
            wallet.debit(amount);
        } catch (InsufficientFundsException e) {
            return false;
        }
        loadedEntity = wallet.mapWalletToEntity(owner);
        walletRepository.save(loadedEntity);
        return true;
    }

    public boolean transfer(UUID sourceWalletId, UUID destinationWalletId, Money amount) {
        UUID firstLock = sourceWalletId.compareTo(destinationWalletId) < 0 ? sourceWalletId : destinationWalletId;
        UUID secondLock = firstLock.equals(sourceWalletId) ? destinationWalletId : sourceWalletId;

        WalletEntity firstWalletEntity = this.loadEntity(firstLock);
        UserEntity firstOwner = userRepository.findById(firstWalletEntity.getOwner().getId()).orElseThrow();
        Wallet firstWallet = firstWalletEntity.mapEntityToWallet(firstOwner.mapEntityToUser());

        WalletEntity secondWalletEntity = this.loadEntity(secondLock);
        UserEntity secondOwner = userRepository.findById(secondWalletEntity.getOwner().getId()).orElseThrow();
        Wallet secondWallet = secondWalletEntity.mapEntityToWallet(secondOwner.mapEntityToUser());

        Wallet sourceWallet = firstLock.equals(sourceWalletId) ? firstWallet : secondWallet;
        Wallet destinationWallet = firstLock.equals(sourceWalletId) ? secondWallet : firstWallet;

        try {
            sourceWallet.debit(amount);
        } catch (InsufficientFundsException e) {
            return false;
        }

        destinationWallet.deposit(amount);

        UserEntity sourceOwner = sourceWallet.getOwner().getId().equals(firstOwner.getId()) ? firstOwner : secondOwner;
        UserEntity destinationOwner = destinationWallet.getOwner().getId().equals(secondOwner.getId()) ? secondOwner : firstOwner;

        WalletEntity sourceEntity = sourceWallet.mapWalletToEntity(sourceOwner);
        WalletEntity destinationEntity = destinationWallet.mapWalletToEntity(destinationOwner);

        walletRepository.save(sourceEntity);
        walletRepository.save(destinationEntity);
        return true;
    }

    private WalletEntity loadEntity(UUID walletId) {
        return walletRepository.findByIdWithLock(walletId);
    }
}
