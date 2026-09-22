package com.householdfinance.finance.domain;

import java.time.LocalDate;
import java.util.UUID;

public final class Budget {
    private final BudgetId id;
    private final HouseholdId householdId;
    private final CategoryId categoryId;
    private final Money limit;
    private Money consumed;
    private final BudgetPeriod period;
    private final LocalDate start;
    private final LocalDate end;

    private Budget(BudgetId id, HouseholdId householdId, CategoryId categoryId, Money limit,
            Money consumed, BudgetPeriod period, LocalDate start, LocalDate end) {
        if (!limit.isPositive() || end.isBefore(start)) {
            throw new DomainException("Invalid budget");
        }
        this.id = id;
        this.householdId = householdId;
        this.categoryId = categoryId;
        this.limit = limit;
        this.consumed = consumed;
        this.period = period;
        this.start = start;
        this.end = end;
    }

    private Budget(HouseholdId householdId, CategoryId categoryId, Money limit,
            BudgetPeriod period, LocalDate start, LocalDate end) {
        this(BudgetId.newId(), householdId, categoryId, limit, Money.zero(limit.currency()), period, start, end);
    }

    public static Budget create(HouseholdId householdId, CategoryId categoryId, Money limit,
            BudgetPeriod period, LocalDate start, LocalDate end) {
        return new Budget(householdId, categoryId, limit, period, start, end);
    }

    public static Budget rehydrate(UUID id, HouseholdId householdId, CategoryId categoryId, Money limit,
            Money consumed, BudgetPeriod period, LocalDate start, LocalDate end) {
        return new Budget(new BudgetId(id), householdId, categoryId, limit, consumed, period, start, end);
    }

    public void consume(Money amount, LocalDate date) {
        if (!amount.isPositive() || date.isBefore(start) || date.isAfter(end)
                || consumed.add(amount).compareTo(limit) > 0) {
            throw new DomainException("Budget limit exceeded");
        }
        consumed = consumed.add(amount);
    }

    public BudgetId id() { return id; }
    public HouseholdId householdId() { return householdId; }
    public CategoryId categoryId() { return categoryId; }
    public Money limit() { return limit; }
    public Money consumed() { return consumed; }
    public BudgetPeriod period() { return period; }
    public LocalDate start() { return start; }
    public LocalDate end() { return end; }
}
