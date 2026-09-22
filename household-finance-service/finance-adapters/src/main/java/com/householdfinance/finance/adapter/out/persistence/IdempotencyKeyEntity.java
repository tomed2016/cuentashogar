package com.householdfinance.finance.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
class IdempotencyKeyEntity {
    @Id UUID id;
    UUID userId;
    String operation;
    String idempotencyKey;
    String requestHash;
    String status;
    UUID resourceId;
    Instant createdAt;
    Instant updatedAt;

    protected IdempotencyKeyEntity() {}
}
