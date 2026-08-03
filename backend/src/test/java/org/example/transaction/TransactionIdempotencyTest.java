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
import java.util.UUID;

@SpringBootTest
@Testcontainers
public class TransactionIdempotencyTest {
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
    public void idempotentWriteCheck() {
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

        UUID idemKey = UUID.randomUUID();
        Transaction transferOne = transactionService.transfer(dude.getId(), idemKey, dudeWallet.getId(), manWallet.getId(), new Money(new BigDecimal("100.00")));
        Transaction transferTwo = transactionService.transfer(man.getId(), idemKey, manWallet.getId(), dudeWallet.getId(), new Money(new BigDecimal("100.00")));

        WalletEntity manLoadedEntity = walletRepository.findById(manWalletEntity.getId()).orElseThrow();
        Wallet manMappedWallet = manLoadedEntity.mapEntityToWallet(man);

        Assertions.assertEquals(transferOne.getId(), transferTwo.getId());
        Assertions.assertEquals(new Money(new BigDecimal("300.00")), manMappedWallet.getBalance());
    }
}
