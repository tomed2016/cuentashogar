package com.householdfinance.finance.adapter.out.persistence;

import com.householdfinance.finance.application.port.out.IdempotencyRepository;
import com.householdfinance.finance.domain.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class IdempotencyRepositoryAdapter implements IdempotencyRepository {
    private final IdempotencyKeyRepository repository;

    public IdempotencyRepositoryAdapter(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public IdempotencyRecord reserve(UUID userId, String operation, String key, String requestHash) {
        var id = UUID.randomUUID();
        var now = Instant.now();
        repository.insertIfAbsent(id, userId, operation, key, requestHash, now);
        var entity = repository.findByUserIdAndOperationAndIdempotencyKey(userId, operation, key)
                .orElseThrow(() -> new IllegalStateException("Idempotency record was not created"));
        return toDomain(entity);
    }

    @Override
    public void complete(UUID recordId, UUID resourceId) {
        var entity = repository.findById(recordId)
                .orElseThrow(() -> new DomainException("Idempotency record not found"));
        entity.status = IdempotencyRecord.Status.COMPLETED.name();
        entity.resourceId = resourceId;
        entity.updatedAt = Instant.now();
        repository.save(entity);
    }

    private IdempotencyRecord toDomain(IdempotencyKeyEntity entity) {
        return new IdempotencyRecord(entity.id, entity.userId, entity.operation,
                entity.idempotencyKey, entity.requestHash,
                IdempotencyRecord.Status.valueOf(entity.status), entity.resourceId, entity.createdAt);
    }
}
