import org.example.exceptions.InsufficientFundsException;
import org.example.exceptions.NegativeMoneyException;
import org.example.money.Money;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MoneyModelTest {
    @Test
    public void throwWhenMoneyNegative() {
        Exception exception = assertThrows(NegativeMoneyException.class,() -> {
            Money meMoney = new Money(new BigDecimal("-1"));
        });
    }

    @Test
    public void allowZeroMoney() {
        assertDoesNotThrow(() -> {
            Money meMoney = new Money(new BigDecimal(BigInteger.ZERO));
        });
    }

    @Test
    public void allowPositiveMoney() {
        assertDoesNotThrow(() -> {
            Money meMoney = new Money(new BigDecimal("123456"));
        });
    }

    @Test
    public void scaleFixed() {
        Money meMoney = new Money(new BigDecimal("12.345"));
        Money expectedMeMoney = new Money(new BigDecimal("12.35"));

        assertEquals(meMoney, expectedMeMoney);
    }

    @Test
    public void addMoney() {
        Money meMoney = new Money(new BigDecimal("10"));
        Money newMeMoney = meMoney.add(new Money(new BigDecimal("90")));

        assertEquals(new Money(new BigDecimal("100")), newMeMoney);
    }

    @Test
    public void throwWhenSubtractingTooMuch() {
        Exception exception = assertThrows(InsufficientFundsException.class, () -> {
            Money meMoney = new Money(new BigDecimal("100"));
            Money badMeMoney = meMoney.subtract(new Money(new BigDecimal("200")));
        });
    }
}
