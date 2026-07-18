package org.example.transaction;

import jakarta.transaction.Transactional;
import org.example.money.Money;
import org.example.user.UserEntity;
import org.example.user.UserRepository;
import org.example.wallet.Wallet;
import org.example.wallet.WalletEntity;
import org.example.wallet.WalletRepository;
import org.example.wallet.WalletService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public TransactionService(WalletService walletService, TransactionRepository transactionRepository, WalletRepository walletRepository, UserRepository userRepository) {
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction deposit(UUID authenticatedUserId, UUID destinationWalletId, Money amount) {
        if(!walletService.verifyOwnership(destinationWalletId, authenticatedUserId))
            throw new IllegalArgumentException("User doesn't own this wallet!");

        Transaction transaction = Transaction.deposit(destinationWalletId, amount);
        TransactionEntity transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            System.out.println("Transfer interrupted");
            transaction.fail();
            transactionEntity = transaction.mapTransactionToEntity();
            transactionRepository.save(transactionEntity);
            return transaction;
        }
        walletService.credit(destinationWalletId, amount);
        transaction.complete();
        transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        return transaction;
    }

    @Transactional
    public Transaction withdraw(UUID authenticatedUserId, UUID sourceWalletId, Money amount) {
        if(!walletService.verifyOwnership(sourceWalletId, authenticatedUserId))
            throw new IllegalArgumentException("User doesn't own this wallet!");

        Transaction transaction = Transaction.withdraw(sourceWalletId, amount);
        TransactionEntity transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            System.out.println("Transfer interrupted");
            transaction.fail();
            transactionEntity = transaction.mapTransactionToEntity();
            transactionRepository.save(transactionEntity);
            return transaction;
        }
        if(walletService.debit(sourceWalletId, amount)) {
            transaction.complete();
        }
        else {
            transaction.fail();
        }

        transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        return transaction;
    }

    @Transactional
    public Transaction transfer(UUID authenticatedUserId, UUID sourceWalletId, UUID destinationWalletId, Money amount) {
        if(!walletService.verifyOwnershipWithoutLock(sourceWalletId, authenticatedUserId))
            throw new IllegalArgumentException("User doesn't own this wallet!");

        Transaction transaction = Transaction.transfer(sourceWalletId, destinationWalletId, amount);
        TransactionEntity transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            System.out.println("Transfer interrupted");
            transaction.fail();
            transactionEntity = transaction.mapTransactionToEntity();
            transactionRepository.save(transactionEntity);
            return transaction;
        }
        if(walletService.transfer(sourceWalletId, destinationWalletId, amount))
            transaction.complete();
        else
            transaction.fail();

        transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        return transaction;
    }
}
