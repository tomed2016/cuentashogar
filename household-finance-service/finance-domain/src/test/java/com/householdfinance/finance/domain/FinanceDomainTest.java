package com.householdfinance.finance.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.*;
import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;

class FinanceDomainTest {

    @Test
    void lastAdminAndCreditRules() {
        var u = UUID.randomUUID();
        var h = Household.create("Home", u, Instant.now());
        assertThatThrownBy(() -> h.removeMember(u, u, Instant.now())).isInstanceOf(DomainException.class);

        var c = CreditCard.issue(h.id(), "Card", Money.of(new BigDecimal("100"), "USD"));
        c.charge(Money.of(new BigDecimal("100"), "USD"), Instant.now());
        assertThatThrownBy(() -> c.charge(Money.of(BigDecimal.ONE, "USD"), Instant.now()))
            .isInstanceOf(DomainException.class);

        // make a payment and then charge again within limit
        c.pay(Money.of(new BigDecimal("50"), "USD"), Instant.now());
        assertThat(c.outstanding().amount()).isEqualByComparingTo("50");
        c.charge(Money.of(new BigDecimal("50"), "USD"), Instant.now());
        assertThat(c.outstanding().amount()).isEqualByComparingTo("100");

        // billing day validation (out of allowed range -> error)
        assertThatThrownBy(() -> CreditCard.issue(h.id(), "Card2", Money.of(new BigDecimal("100"), "USD"), 29))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void budgetConsumptionIsBounded() {
        var b = Budget.create(HouseholdId.newId(), CategoryId.newId(), Money.of(new BigDecimal("10"), "USD"), BudgetPeriod.MONTHLY, LocalDate.now(), LocalDate.now());
        b.consume(Money.of(new BigDecimal("4"), "USD"), LocalDate.now());
        assertThat(b.consumed().amount()).isEqualByComparingTo("4");
        assertThatThrownBy(() -> b.consume(Money.of(new BigDecimal("7"), "USD"), LocalDate.now())).isInstanceOf(DomainException.class);
    }

    @Test
    void memberRoleCompatibility() {
        var m = new HouseholdMember(UUID.randomUUID(), MemberRole.HOUSEHOLD_ADMIN);
        assertThat(m.admin()).isTrue();
    }
}
