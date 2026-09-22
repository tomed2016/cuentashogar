create table idempotency_keys(
    id uuid primary key,
    user_id uuid not null,
    operation varchar(80) not null,
    idempotency_key varchar(200) not null,
    request_hash varchar(64) not null,
    status varchar(20) not null,
    resource_id uuid,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique(user_id, operation, idempotency_key)
);

create index ix_idempotency_created_at on idempotency_keys(created_at);
