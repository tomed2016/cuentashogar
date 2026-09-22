package com.householdfinance.finance.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, UUID> {
    Optional<IdempotencyKeyEntity> findByUserIdAndOperationAndIdempotencyKey(
            UUID userId, String operation, String idempotencyKey);

    @Modifying
    @Query(value = """
            insert into idempotency_keys
                (id, user_id, operation, idempotency_key, request_hash, status, created_at, updated_at)
            values
                (:id, :userId, :operation, :idempotencyKey, :requestHash, 'PROCESSING', :createdAt, :createdAt)
            on conflict (user_id, operation, idempotency_key) do nothing
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("userId") UUID userId,
                       @Param("operation") String operation,
                       @Param("idempotencyKey") String idempotencyKey,
                       @Param("requestHash") String requestHash,
                       @Param("createdAt") Instant createdAt);
}
