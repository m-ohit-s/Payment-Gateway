package com.myspace.razorpayclone.common.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Money{
    private int amountUnits;
    private String currency;

    public static Money of(int amountUnits, String currency) {
        return new Money(amountUnits, currency);
    }

    public static Money inr(int amountUnits) {
        return new Money(amountUnits, "INR");
    }

    public Money add(Money money) {
        if (currency == null || !currency.equals(money.currency)) {
            throw new IllegalArgumentException("Currency has invalid value");
        }
        return new Money(amountUnits + money.amountUnits, currency);
    }

    public Money subtract(Money money) {
        if (currency == null || !currency.equals(money.currency)) {
            throw new IllegalArgumentException("Currency has invalid value");
        }
        return new Money(amountUnits - money.amountUnits, currency);
    }
}
