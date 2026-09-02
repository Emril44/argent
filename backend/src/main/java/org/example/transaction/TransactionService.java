package org.example.transaction;

import jakarta.transaction.Transactional;
import org.example.money.Money;
import org.example.wallet.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransactionService(WalletService walletService, TransactionRepository transactionRepository, ApplicationEventPublisher publisher) {
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = publisher;
    }

    @Transactional
    public Transaction deposit(UUID authenticatedUserId, UUID idempotencyKey, UUID destinationWalletId, Money amount) {
        Optional<TransactionEntity> dupeTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if(dupeTransaction.isPresent()) {
            return dupeTransaction.get().mapEntityToTransaction();
        }

        if(!walletService.verifyOwnership(destinationWalletId, authenticatedUserId)) {
            throw new IllegalArgumentException("User doesn't own this wallet!");
        }

        Transaction transaction = Transaction.deposit(destinationWalletId, idempotencyKey, amount);
        TransactionEntity transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            System.out.println("Transfer interrupted");
            transaction.fail();
            transactionEntity = transaction.mapTransactionToEntity();
            transactionRepository.save(transactionEntity);
            eventPublisher.publishEvent(new TransactionCompletedEvent(authenticatedUserId, transaction.getId(), transaction.getProcessedAt().orElseThrow(), TransactionStatus.FAILED));
            return transaction;
        }
        walletService.credit(destinationWalletId, amount);
        transaction.complete();
        transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        eventPublisher.publishEvent(new TransactionCompletedEvent(authenticatedUserId, transaction.getId(), transaction.getProcessedAt().orElseThrow(), TransactionStatus.SUCCESS));
        return transaction;
    }

    @Transactional
    public Transaction withdraw(UUID authenticatedUserId, UUID idempotencyKey, UUID sourceWalletId, Money amount) {
        Optional<TransactionEntity> dupeTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if(dupeTransaction.isPresent()) {
            return dupeTransaction.get().mapEntityToTransaction();
        }

        if(!walletService.verifyOwnership(sourceWalletId, authenticatedUserId)) {
            throw new IllegalArgumentException("User doesn't own this wallet!");
        }

        Transaction transaction = Transaction.withdraw(sourceWalletId, idempotencyKey, amount);
        TransactionEntity transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            System.out.println("Transfer interrupted");
            transaction.fail();
            transactionEntity = transaction.mapTransactionToEntity();
            transactionRepository.save(transactionEntity);
            eventPublisher.publishEvent(new TransactionCompletedEvent(authenticatedUserId, transaction.getId(), transaction.getProcessedAt().orElseThrow(), TransactionStatus.FAILED));
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

        eventPublisher.publishEvent(new TransactionCompletedEvent(authenticatedUserId, transaction.getId(), transaction.getProcessedAt().orElseThrow(), TransactionStatus.SUCCESS));
        return transaction;
    }

    @Transactional
    public Transaction transfer(UUID authenticatedUserId, UUID idempotencyKey, UUID sourceWalletId, UUID destinationWalletId, Money amount) {
        Optional<TransactionEntity> dupeTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if(dupeTransaction.isPresent()) {
            return dupeTransaction.get().mapEntityToTransaction();
        }

        if(!walletService.verifyOwnershipWithoutLock(sourceWalletId, authenticatedUserId)) {
            throw new IllegalArgumentException("User doesn't own this wallet!");
        }

        Transaction transaction = Transaction.transfer(sourceWalletId, destinationWalletId, idempotencyKey, amount);
        TransactionEntity transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            System.out.println("Transfer interrupted");
            transaction.fail();
            transactionEntity = transaction.mapTransactionToEntity();
            transactionRepository.save(transactionEntity);
            eventPublisher.publishEvent(new TransactionCompletedEvent(authenticatedUserId, transaction.getId(), transaction.getProcessedAt().orElseThrow(), TransactionStatus.FAILED));
            return transaction;
        }
        if(walletService.transfer(sourceWalletId, destinationWalletId, amount))
            transaction.complete();
        else
            transaction.fail();

        transactionEntity = transaction.mapTransactionToEntity();
        transactionRepository.save(transactionEntity);

        eventPublisher.publishEvent(new TransactionCompletedEvent(authenticatedUserId, transaction.getId(), transaction.getProcessedAt().orElseThrow(), TransactionStatus.SUCCESS));
        return transaction;
    }

    public Transaction fetchTransaction(UUID transId) {
        TransactionEntity foundEntity = transactionRepository.findById(transId).orElseThrow();

        return foundEntity.mapEntityToTransaction();
    }
}
