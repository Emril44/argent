import org.example.exceptions.NegativeMoneyException;
import org.example.money.Money;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class NegativeMoneyTest {
    @Test
    public void throwWhenMoneyNegative() {
        Exception exception = assertThrows(NegativeMoneyException.class,() -> {
            Money meMoney = new Money(new BigDecimal("-1"));
        });

        String expectedMessage = "cannot be negative";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
