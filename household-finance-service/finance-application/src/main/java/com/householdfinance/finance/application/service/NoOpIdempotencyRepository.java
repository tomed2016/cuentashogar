package com.householdfinance.finance.application.service;

import com.householdfinance.finance.application.port.out.IdempotencyRepository;
import java.time.Instant;
import java.util.UUID;

final class NoOpIdempotencyRepository implements IdempotencyRepository {
    @Override
    public IdempotencyRecord reserve(UUID userId, String operation, String key, String requestHash) {
        return new IdempotencyRecord(UUID.randomUUID(), userId, operation, key, requestHash,
                IdempotencyRecord.Status.PROCESSING, null, Instant.now());
    }

    @Override
    public void complete(UUID recordId, UUID resourceId) {
        // Used only by the in-memory unit-test factory.
    }
}
