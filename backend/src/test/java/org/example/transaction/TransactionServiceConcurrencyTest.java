package org.example.transaction;

import org.example.money.Money;
import org.example.user.User;
import org.example.user.UserEntity;
import org.example.user.UserRepository;
import org.example.wallet.Wallet;
import org.example.wallet.WalletEntity;
import org.example.wallet.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@Testcontainers
public class TransactionServiceConcurrencyTest {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private UserRepository userRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16"
    );

    @BeforeEach
    public void clearDatabase() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void concurrentWithdrawalFail() throws InterruptedException {
        User dude = new User("yef", "yef@yef.yef", "123123123");
        UserEntity dudeEntity = dude.mapUserToEntity();
        Wallet dudeWallet = dude.openWallet();
        userRepository.save(dudeEntity);
        dudeWallet.deposit(new Money(new BigDecimal("200.00")));
        WalletEntity dudeWalletEntity = dudeWallet.mapWalletToEntity(dudeEntity);
        walletRepository.save(dudeWalletEntity);

        List<Transaction> finishedTransactions = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(2);
        for(int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Transaction transaction = transactionService.withdraw(dude.getId(), dudeWallet.getId(), new Money(new BigDecimal("150.00")));
                    finishedTransactions.add(transaction);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        // Execute both threads at once
        startLatch.countDown();
        completeLatch.await();

        Assertions.assertEquals(1, finishedTransactions.stream().filter(t -> t.getTransactionStatus() == TransactionStatus.SUCCESS).count());
        Assertions.assertEquals(1, finishedTransactions.stream().filter(t -> t.getTransactionStatus() == TransactionStatus.FAILED).count());

        WalletEntity loadedEntity = walletRepository.findById(dudeWalletEntity.getId()).orElseThrow();
        Wallet mappedWallet = loadedEntity.mapEntityToWallet(dude);
        Assertions.assertEquals(new Money(new BigDecimal("50.00")), mappedWallet.getBalance());
    }

    @Test
    public void transferDeadlockSuccessTest() throws InterruptedException {
        User dude = new User("yef", "yef@yef.yef", "123123123");
        UserEntity dudeEntity = dude.mapUserToEntity();
        Wallet dudeWallet = dude.openWallet();
        userRepository.save(dudeEntity);
        dudeWallet.deposit(new Money(new BigDecimal("200.00")));
        WalletEntity dudeWalletEntity = dudeWallet.mapWalletToEntity(dudeEntity);
        walletRepository.save(dudeWalletEntity);

        User man = new User("jaj", "jaj@jaj.jaj", "jajajaja.");
        UserEntity manEntity = man.mapUserToEntity();
        Wallet manWallet = man.openWallet();
        userRepository.save(manEntity);
        manWallet.deposit(new Money(new BigDecimal("200.00")));
        WalletEntity manWalletEntity = manWallet.mapWalletToEntity(manEntity);
        walletRepository.save(manWalletEntity);

        List<Transaction> finishedTransactions = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(2);
        executor.submit(() -> {
            try {
                startLatch.await();
                Transaction transferOne = transactionService.transfer(dude.getId(), dudeWallet.getId(), manWallet.getId(), new Money(new BigDecimal("100.00")));
                finishedTransactions.add(transferOne);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completeLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                Transaction transferTwo = transactionService.transfer(man.getId(), manWallet.getId(), dudeWallet.getId(), new Money(new BigDecimal("100.00")));
                finishedTransactions.add(transferTwo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completeLatch.countDown();
            }
        });
        // Execute both threads at once
        startLatch.countDown();
        completeLatch.await();

        Assertions.assertEquals(2, finishedTransactions.stream().filter(t -> t.getTransactionStatus() == TransactionStatus.SUCCESS).count());

        WalletEntity dudeLoadedEntity = walletRepository.findById(dudeWalletEntity.getId()).orElseThrow();
        Wallet dudeMappedWallet = dudeLoadedEntity.mapEntityToWallet(dude);
        Assertions.assertEquals(new Money(new BigDecimal("200.00")), dudeMappedWallet.getBalance());

        WalletEntity manLoadedEntity = walletRepository.findById(manWalletEntity.getId()).orElseThrow();
        Wallet manMappedWallet = manLoadedEntity.mapEntityToWallet(man);
        Assertions.assertEquals(new Money(new BigDecimal("200.00")), manMappedWallet.getBalance());
    }
}
