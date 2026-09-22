package com.householdfinance.finance.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class CreditCard {

    private final CreditCardId id;
    private final HouseholdId householdId;
    private final String name;
    private final Money creditLimit;
    private Money outstanding;
    private final int billingDay;

    private CreditCard(CreditCardId id, HouseholdId h, String n, Money l, int billingDay) {
        this.id = id;
        householdId = Objects.requireNonNull(h);
        name = Objects.requireNonNull(n);
        creditLimit = Objects.requireNonNull(l);
        if (!l.isPositive()) throw new DomainException("Credit limit must be positive");
        if (billingDay < 1 || billingDay > 28) throw new DomainException("Billing day must be between 1 and 28");
        this.billingDay = billingDay;
        outstanding = Money.zero(l.currency());
    }

    public static CreditCard issue(HouseholdId h, String n, Money l) {
        return new CreditCard(CreditCardId.newId(), h, n, l, 1);
    }

    public static CreditCard issue(HouseholdId h, String n, Money l, int billingDay) {
        return new CreditCard(CreditCardId.newId(), h, n, l, billingDay);
    }

    public static CreditCard rehydrate(UUID id, HouseholdId householdId, String name, Money limit,
            Money outstanding, int billingDay, boolean active) {
        var card = new CreditCard(new CreditCardId(id), householdId, name, limit, billingDay);
        card.outstanding = outstanding;
        return card;
    }

    public void charge(Money a, Instant i) {
        if (!a.isPositive()) throw new DomainException("Charge amount must be positive");
        if (!a.currency().equals(creditLimit.currency())) throw new DomainException("Currency mismatch");
        if (outstanding.add(a).compareTo(creditLimit) > 0) throw new DomainException("Credit limit exceeded");
        outstanding = outstanding.add(a);
    }

    public void pay(Money a, Instant i) {
        if (!a.isPositive()) throw new DomainException("Payment amount must be positive");
        if (!a.currency().equals(creditLimit.currency())) throw new DomainException("Currency mismatch");
        if (a.compareTo(outstanding) > 0) throw new DomainException("Payment exceeds outstanding");
        outstanding = outstanding.subtract(a);
    }

    public CreditCardId id() {
        return id;
    }

    public HouseholdId householdId() {
        return householdId;
    }

    public String name() {
        return name;
    }

    public Money creditLimit() {
        return creditLimit;
    }

    public Money outstanding() {
        return outstanding;
    }

    public int billingDay() {
        return billingDay;
    }
}
