import org.example.exceptions.IllegalStatusTransitionException;
import org.example.money.Money;
import org.example.transaction.Transaction;
import org.example.user.User;
import org.example.wallet.Wallet;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TransactionModelTest {
    @Test
    public void correctDepositInit() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        assertDoesNotThrow(() -> Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00"))));
    }

    @Test
    public void correctWithdrawInit() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));
        assertDoesNotThrow(() -> Transaction.withdraw(guyWallet.getId(), new Money(new BigDecimal("100.00"))));
    }

    @Test
    public void correctTransferInit() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();
        Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        User dude = new User("fofe", "fofe@fofe.fofe", "lkdjnbldkjfgsytuyhr");
        Wallet dudeWallet = dude.openWallet();

        assertDoesNotThrow(() -> Transaction.transfer(guyWallet.getId(), dudeWallet.getId(), new Money(new BigDecimal("100.00"))));
    }

    @Test
    public void throwWhenSameWalletTransfer() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        assertThrows(IllegalArgumentException.class, () -> Transaction.transfer(guyWallet.getId(), guyWallet.getId(), new Money(new BigDecimal("1.00"))));
    }

    @Test
    public void completeTransaction() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction trans = Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        assertDoesNotThrow(trans::complete);
    }

    @Test
    public void failTransaction() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction trans = Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        assertDoesNotThrow(trans::fail);
    }

    @Test
    public void badCompleteTransaction() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction trans = Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        trans.fail();
        assertThrows(IllegalStatusTransitionException.class, trans::complete);
    }

    @Test
    public void badFailTransaction() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction trans = Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        trans.complete();
        assertThrows(IllegalStatusTransitionException.class, trans::fail);
    }

    @Test
    public void badDoubleCompleteTransaction() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction trans = Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        trans.complete();
        assertThrows(IllegalStatusTransitionException.class, trans::complete);
    }

    @Test
    public void badDoubleFailTransaction() {
        User guy = new User("feef", "feef@feef.feef", "sarlifhgvbwoaesldfhobg");
        Wallet guyWallet = guy.openWallet();

        Transaction trans = Transaction.deposit(guyWallet.getId(), new Money(new BigDecimal("100.00")));

        trans.fail();
        assertThrows(IllegalStatusTransitionException.class, trans::fail);
    }
}
