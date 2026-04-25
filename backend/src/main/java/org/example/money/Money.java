package org.example.money;

import org.example.exceptions.NegativeMoneyException;
import java.math.BigDecimal;

public class Money {
    private final BigDecimal moneyAmount;
    public Money(BigDecimal moneyAmount) throws RuntimeException {
        if(moneyAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new NegativeMoneyException("Money cannot be negative.");

        this.moneyAmount = moneyAmount;
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }
}
