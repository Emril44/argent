package org.example.money;

import org.example.exceptions.InsufficientFundsException;
import org.example.exceptions.NegativeMoneyException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    private final BigDecimal moneyAmount;
    public Money(BigDecimal moneyAmount) throws NegativeMoneyException {
        if(moneyAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new NegativeMoneyException("Money cannot be negative.");

        this.moneyAmount = moneyAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return moneyAmount.compareTo(money.moneyAmount) == 0;
    }

    public boolean isZero() {
        return this.getMoneyAmount().compareTo(new BigDecimal("0.00")) == 0;
    }

    @Override
    public int hashCode() {
        BigDecimal strippedMoney = moneyAmount.stripTrailingZeros();
        return Objects.hashCode(strippedMoney);
    }

    public Money add(Money addedMoney) {
        BigDecimal currentMoney = this.getMoneyAmount();
        BigDecimal newMoney = addedMoney.getMoneyAmount();

        return new Money(currentMoney.add(newMoney));
    }

    public Money subtract(Money subtractedMoney) {
        BigDecimal currentMoney = this.getMoneyAmount();
        BigDecimal subtractAmount = subtractedMoney.getMoneyAmount();

        if(subtractAmount.compareTo(currentMoney) > 0) {
            throw new InsufficientFundsException("Attempted to subtract " + subtractAmount + "from " + currentMoney + ". Negative balance is not allowed.");
        }
        return new Money(currentMoney.subtract(subtractAmount));
    }
}
