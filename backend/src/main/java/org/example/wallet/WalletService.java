package org.example.wallet;

import jakarta.transaction.Transactional;
import org.example.exceptions.InsufficientFundsException;
import org.example.money.Money;
import org.example.user.User;
import org.example.user.UserEntity;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.rmi.NoSuchObjectException;
import java.util.MissingResourceException;
import java.util.Optional;
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

    public boolean verifyOwnershipWithoutLock(UUID walletId, UUID authenticatedUserId) {
        Optional<UUID> userWalletId = walletRepository.findUserWalletId(walletId, authenticatedUserId);
        return userWalletId.isPresent();
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

    @Transactional
    public Wallet createWallet(UUID userId) {
        UserEntity foundUser = userRepository.findById(userId).orElseThrow();
        User mappedUser = foundUser.mapEntityToUser();
        Wallet newWallet = mappedUser.openWallet();
        walletRepository.save(newWallet.mapWalletToEntity(foundUser));
        return newWallet;
    }

    public Wallet getWallet(UUID walletId, UUID userId) throws AccessDeniedException {
        WalletEntity foundWallet = walletRepository.findById(walletId).orElseThrow();
        UserEntity foundUser = userRepository.findById(userId).orElseThrow();
        if(!foundWallet.getOwner().getId().equals(foundUser.getId())) {
            throw new AccessDeniedException("User doesn't own this wallet!");
        }

        User correctUser = foundUser.mapEntityToUser();
        return foundWallet.mapEntityToWallet(correctUser);
    }

    @Transactional
    public Wallet freezeWallet(UUID walletId, UUID userId) throws AccessDeniedException {
        Result result = getResult(walletId, userId);

        User correctUser = result.foundUser.mapEntityToUser();
        Wallet correctWallet = result.foundWallet.mapEntityToWallet(correctUser);
        correctWallet.freeze();
        walletRepository.save(correctWallet.mapWalletToEntity(result.foundUser));
        return correctWallet;
    }

    @Transactional
    public Wallet unfreezeWallet(UUID walletId, UUID userId) throws AccessDeniedException {
        Result result = getResult(walletId, userId);

        User correctUser = result.foundUser().mapEntityToUser();
        Wallet correctWallet = result.foundWallet().mapEntityToWallet(correctUser);
        correctWallet.unfreeze();
        walletRepository.save(correctWallet.mapWalletToEntity(result.foundUser()));
        return correctWallet;
    }

    private Result getResult(UUID walletId, UUID userId) throws AccessDeniedException {
        WalletEntity foundWallet = walletRepository.findByIdWithLock(walletId);
        UserEntity foundUser = userRepository.findById(userId).orElseThrow();
        if(!foundWallet.getOwner().getId().equals(foundUser.getId())) {
            throw new AccessDeniedException("User doesn't own this wallet!");
        }
        Result result = new Result(foundWallet, foundUser);
        return result;
    }

    private record Result(WalletEntity foundWallet, UserEntity foundUser) {
    }

    private WalletEntity loadEntity(UUID walletId) {
        return walletRepository.findByIdWithLock(walletId);
    }
}
