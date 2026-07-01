package org.example.money;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class MoneyEntity {
    @Column(name = "balance")
    private BigDecimal amount;

    public MoneyEntity() {
        this.amount = new BigDecimal("0.00");
    }

    public MoneyEntity(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money mapEntityToMoney() {
        return new Money(this.getAmount());
    }

    public static MoneyEntity mapMoneyToEntity(Money money) {
        return new MoneyEntity((money.getMoneyAmount()));
    }
}
