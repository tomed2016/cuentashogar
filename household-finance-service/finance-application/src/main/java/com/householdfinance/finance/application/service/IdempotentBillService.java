package com.householdfinance.finance.application.service;

import com.householdfinance.finance.application.port.in.BillUseCase;
import com.householdfinance.finance.application.port.out.FinanceRepository;
import com.householdfinance.finance.application.port.out.IdempotencyRepository;
import com.householdfinance.finance.domain.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

final class IdempotentBillService implements BillUseCase {
    private static final String OPERATION = "BILL_PAYMENT";
    private final BillUseCase delegate;
    private final FinanceRepository finance;
    private final IdempotencyRepository idempotency;

    IdempotentBillService(BillUseCase delegate, FinanceRepository finance, IdempotencyRepository idempotency) {
        this.delegate = delegate;
        this.finance = finance;
        this.idempotency = idempotency;
    }

    @Override
    public HouseholdBill create(UUID userId, HouseholdId householdId, String name, Money amount, LocalDate dueDate) {
        return delegate.create(userId, householdId, name, amount, dueDate);
    }

    @Override
    public BillOccurrence occurrence(UUID userId, BillId billId, LocalDate dueDate) {
        return delegate.occurrence(userId, billId, dueDate);
    }

    @Override
    public BillOccurrence pay(UUID userId, OccurrenceId occurrenceId, AccountId accountId, String description) {
        return delegate.pay(userId, occurrenceId, accountId, description);
    }

    @Override
    @Transactional
    public BillOccurrence pay(UUID userId, OccurrenceId occurrenceId, AccountId accountId,
                              String description, String idempotencyKey) {
        validateKey(idempotencyKey);
        var hash = requestHash(occurrenceId, accountId, description);
        var record = idempotency.reserve(userId, OPERATION, idempotencyKey, hash);
        if (!record.requestHash().equals(hash)) {
            throw new DomainException("Idempotency-Key was already used with a different request");
        }
        if (record.status() == IdempotencyRepository.IdempotencyRecord.Status.COMPLETED) {
            return finance.occurrence(new OccurrenceId(record.resourceId()))
                    .orElseThrow(() -> new DomainException("Idempotent payment result not found"));
        }
        if (record.status() == IdempotencyRepository.IdempotencyRecord.Status.PROCESSING) {
            throw new DomainException("Idempotent payment is already in progress");
        }

        var paid = delegate.pay(userId, occurrenceId, accountId, description);
        idempotency.complete(record.id(), paid.id().value());
        return paid;
    }

    @Override
    public List<HouseholdBill> list(UUID userId, HouseholdId householdId) {
        return delegate.list(userId, householdId);
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank() || key.length() > 200) {
            throw new DomainException("Idempotency-Key must contain between 1 and 200 characters");
        }
    }

    private String requestHash(OccurrenceId occurrenceId, AccountId accountId, String description) {
        var value = occurrenceId.value() + "|" + accountId.value() + "|" + (description == null ? "" : description);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
