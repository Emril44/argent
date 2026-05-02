import org.example.exceptions.IllegalStatusTransitionException;
import org.example.exceptions.InsufficientFundsException;
import org.example.money.Money;
import org.example.user.User;
import org.example.wallet.Wallet;
import org.example.wallet.WalletStatus;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class WalletModelTest {
    String name = "feef";
    String email = "feef@feef.feef";
    String passwordHash = "asldrhgvq2i34oyuh56gb2lk345,.rt";

    User guy = new User(name, email, passwordHash);
    @Test
    public void walletStartBalance() {
        Wallet meMoneyHolder = new Wallet(guy);
        assertEquals(new Money(new BigDecimal("0.00")), meMoneyHolder.getBalance());
    }

    @Test
    public void activeToFrozen() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.freeze();
        assertEquals(WalletStatus.FROZEN, meMoneyHolder.getStatus());
    }

    @Test
    public void activeToClosed() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.close();
        assertEquals(WalletStatus.CLOSED, meMoneyHolder.getStatus());
    }

    @Test
    public void frozenToActive() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.freeze();

        meMoneyHolder.unfreeze();
        assertEquals(WalletStatus.ACTIVE, meMoneyHolder.getStatus());
    }

    @Test
    public void frozenToClosed() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.freeze();

        assertThrows(IllegalStatusTransitionException.class, meMoneyHolder::close);
    }

    @Test
    public void closedToFrozen() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.close();

        assertThrows(IllegalStatusTransitionException.class, meMoneyHolder::freeze);
    }

    @Test
    public void depositTest() {
        Wallet meMoneyHolder = new Wallet(guy);
        assertDoesNotThrow(() -> meMoneyHolder.deposit(new Money(new BigDecimal("100.00"))));

        assertEquals(new Money(new BigDecimal("100.00")), meMoneyHolder.getBalance());
    }

    @Test
    public void goodDebitTest() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.deposit(new Money(new BigDecimal("100.00")));
        assertDoesNotThrow(() -> meMoneyHolder.debit(new Money(new BigDecimal("90.00"))));

        assertEquals(new Money(new BigDecimal("10.00")), meMoneyHolder.getBalance());
    }

    @Test
    public void badDebitTest() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.deposit(new Money(new BigDecimal("100.00")));
        assertThrows(InsufficientFundsException.class, () -> meMoneyHolder.debit(new Money(new BigDecimal("1000.00"))));
    }

    @Test
    public void closedToActive() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.close();

        assertThrows(IllegalStatusTransitionException.class, meMoneyHolder::unfreeze);
    }

    @Test
    public void depositToFrozen() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.freeze();

        assertThrows(IllegalStatusTransitionException.class, () -> meMoneyHolder.deposit(new Money(new BigDecimal("1.00"))));
    }

    @Test
    public void debitFromFrozen() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.deposit(new Money(new BigDecimal("5.00")));
        meMoneyHolder.freeze();

        assertThrows(IllegalStatusTransitionException.class, () -> meMoneyHolder.debit(new Money(new BigDecimal("1.00"))));
    }

    @Test
    public void depositToClosed() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.close();

        assertThrows(IllegalStatusTransitionException.class, () -> meMoneyHolder.deposit(new Money(new BigDecimal("1.00"))));
    }

    @Test
    public void debitFromClosed() {
        Wallet meMoneyHolder = new Wallet(guy);
        meMoneyHolder.close();

        assertThrows(IllegalStatusTransitionException.class, () -> meMoneyHolder.debit(new Money(new BigDecimal("1.00"))));
    }
}
