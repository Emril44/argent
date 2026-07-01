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

    private WalletEntity loadEntity(UUID walletId) {
        return walletRepository.findByIdWithLock(walletId);
    }
}
