package com.householdfinance.finance.domain;

import java.time.Instant;
import java.util.UUID;

public final class Transaction {
    private final TransactionId id;
    private final HouseholdId householdId;
    private final AccountId accountId;
    private final AccountId destinationAccountId;
    private final UUID categoryId;
    private final TransactionType type;
    private final Money amount;
    private final Instant occurredAt;
    private final String description;
    private boolean reversed;
    private TransactionId reversalOf;

    private Transaction(TransactionId id, HouseholdId householdId, AccountId accountId,
            AccountId destinationAccountId, UUID categoryId, TransactionType type, Money amount,
            Instant occurredAt, String description) {
        this.id = id;
        this.householdId = householdId;
        this.accountId = accountId;
        this.destinationAccountId = destinationAccountId;
        this.categoryId = categoryId;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.description = description;
    }

    public static Transaction record(HouseholdId householdId, AccountId accountId, UUID categoryId,
            TransactionType type, Money amount, Instant occurredAt, String description) {
        if (!amount.isPositive() || type == TransactionType.TRANSFER) {
            throw new DomainException("Invalid transaction");
        }
        return new Transaction(TransactionId.newId(), householdId, accountId, null, categoryId,
                type, amount, occurredAt, description);
    }

    public static Transaction transfer(HouseholdId householdId, AccountId from, AccountId to,
            Money amount, Instant occurredAt, String description) {
        if (from.equals(to) || !amount.isPositive()) {
            throw new DomainException("Invalid transfer");
        }
        return new Transaction(TransactionId.newId(), householdId, from, to, null,
                TransactionType.TRANSFER, amount, occurredAt, description);
    }

    public static Transaction reversalOf(Transaction original, Instant occurredAt) {
        if (original.reversed) {
            throw new DomainException("Transaction already reversed");
        }
        var reversal = new Transaction(TransactionId.newId(), original.householdId, original.accountId,
                original.destinationAccountId, original.categoryId, TransactionType.REVERSAL,
                original.amount, occurredAt, "Reversal of " + original.id);
        reversal.reversalOf = original.id;
        return reversal;
    }

    public static Transaction rehydrate(UUID id, HouseholdId householdId, AccountId accountId,
            AccountId destinationAccountId, UUID categoryId, TransactionType type, Money amount,
            Instant occurredAt, String description, boolean reversed) {
        var transaction = new Transaction(new TransactionId(id), householdId, accountId,
                destinationAccountId, categoryId, type, amount, occurredAt, description);
        transaction.reversed = reversed;
        return transaction;
    }

    public void markReversed() {
        if (reversed) {
            throw new DomainException("Transaction already reversed");
        }
        reversed = true;
    }

    public TransactionId id() { return id; }
    public HouseholdId householdId() { return householdId; }
    public AccountId accountId() { return accountId; }
    public AccountId destinationAccountId() { return destinationAccountId; }
    public UUID categoryId() { return categoryId; }
    public TransactionType type() { return type; }
    public Money amount() { return amount; }
    public Instant occurredAt() { return occurredAt; }
    public String description() { return description; }
    public boolean reversed() { return reversed; }
    public TransactionId reversalOf() { return reversalOf; }
}
