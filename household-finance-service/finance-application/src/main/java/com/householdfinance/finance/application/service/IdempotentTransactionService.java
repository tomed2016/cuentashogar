package com.householdfinance.finance.application.service;

import com.householdfinance.finance.application.port.in.TransactionUseCase;
import com.householdfinance.finance.application.port.out.FinanceRepository;
import com.householdfinance.finance.application.port.out.IdempotencyRepository;
import com.householdfinance.finance.domain.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

final class IdempotentTransactionService implements TransactionUseCase {
    private final TransactionUseCase delegate;
    private final FinanceRepository finance;
    private final IdempotencyRepository idempotency;

    IdempotentTransactionService(TransactionUseCase delegate, FinanceRepository finance,
                                 IdempotencyRepository idempotency) {
        this.delegate = delegate;
        this.finance = finance;
        this.idempotency = idempotency;
    }

    @Override
    public Transaction record(UUID userId, HouseholdId householdId, AccountId accountId, UUID categoryId,
                              TransactionType type, Money amount, Instant occurredAt, String description) {
        return delegate.record(userId, householdId, accountId, categoryId, type, amount, occurredAt, description);
    }

    @Override
    @Transactional
    public Transaction record(UUID userId, HouseholdId householdId, AccountId accountId, UUID categoryId,
                              TransactionType type, Money amount, Instant occurredAt, String description,
                              String idempotencyKey) {
        var record = reserve(userId, "TRANSACTION_" + type.name(),
                idempotencyKey, accountId.value() + "|" + categoryId + "|" + type + "|" + amount + "|"
                        + description);
        if (record.status() == IdempotencyRepository.IdempotencyRecord.Status.COMPLETED) {
            return transaction(record.resourceId());
        }
        if (record.status() == IdempotencyRepository.IdempotencyRecord.Status.PROCESSING) {
            throw new DomainException("Idempotent transaction is already in progress");
        }
        var result = delegate.record(userId, householdId, accountId, categoryId, type, amount, occurredAt, description);
        idempotency.complete(record.id(), result.id().value());
        return result;
    }

    @Override
    public Transaction transfer(UUID userId, HouseholdId householdId, AccountId from, AccountId to,
                                Money amount, Instant occurredAt, String description) {
        return delegate.transfer(userId, householdId, from, to, amount, occurredAt, description);
    }

    @Override
    @Transactional
    public Transaction transfer(UUID userId, HouseholdId householdId, AccountId from, AccountId to,
                                Money amount, Instant occurredAt, String description, String idempotencyKey) {
        var record = reserve(userId, "TRANSFER",
                idempotencyKey, from.value() + "|" + to.value() + "|" + amount + "|" + description);
        if (record.status() == IdempotencyRepository.IdempotencyRecord.Status.COMPLETED) {
            return transaction(record.resourceId());
        }
        if (record.status() == IdempotencyRepository.IdempotencyRecord.Status.PROCESSING) {
            throw new DomainException("Idempotent transfer is already in progress");
        }
        var result = delegate.transfer(userId, householdId, from, to, amount, occurredAt, description);
        idempotency.complete(record.id(), result.id().value());
        return result;
    }

    @Override
    public List<Transaction> list(UUID userId, HouseholdId householdId) {
        return delegate.list(userId, householdId);
    }

    private IdempotencyRepository.IdempotencyRecord reserve(UUID userId, String operation,
                                                              String key, String input) {
        if (key == null || key.isBlank() || key.length() > 200) {
            throw new DomainException("Idempotency-Key must contain between 1 and 200 characters");
        }
        var hash = sha256(input);
        var record = idempotency.reserve(userId, operation, key, hash);
        if (!record.requestHash().equals(hash)) {
            throw new DomainException("Idempotency-Key was already used with a different request");
        }
        return record;
    }

    private Transaction transaction(UUID id) {
        return finance.transaction(new TransactionId(id))
                .orElseThrow(() -> new DomainException("Idempotent transaction result not found"));
    }

    private String sha256(String input) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
