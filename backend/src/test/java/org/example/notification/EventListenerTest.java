package org.example.notification;

import org.example.money.Money;
import org.example.transaction.Transaction;
import org.example.transaction.TransactionCompletedEvent;
import org.example.transaction.TransactionRepository;
import org.example.transaction.TransactionService;
import org.example.user.*;
import org.example.wallet.Wallet;
import org.example.wallet.WalletEntity;
import org.example.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
public class EventListenerTest {
    @Autowired
    private UserService userService;
    @Autowired
    ApplicationEvents events;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    public void clearDatabase() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void registerUser() {
        userService.register("feef", "feef@feef", "1234512435");
        long numEvents = events.stream(UserRegisteredEvent.class).count();
        assertThat(numEvents).isEqualTo(1);
    }

    @Test
    public void completeTransaction() {
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

        transactionService.deposit(dude.getId(), UUID.randomUUID(), dudeWallet.getId(), new Money(new BigDecimal("10.00")));
        transactionService.withdraw(dude.getId(), UUID.randomUUID(), dudeWallet.getId(), new Money(new BigDecimal("10.00")));
        transactionService.transfer(dude.getId(), UUID.randomUUID(), dudeWallet.getId(), manWallet.getId(), new Money(new BigDecimal("10.00")));

        long numEvents = events.stream(TransactionCompletedEvent.class).count();
        assertThat(numEvents).isEqualTo(3);
    }
}
