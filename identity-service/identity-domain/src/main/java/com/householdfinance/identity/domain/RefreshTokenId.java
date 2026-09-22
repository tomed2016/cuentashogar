package com.householdfinance.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Identificador unico de un refresh token emitido. */
public record RefreshTokenId(UUID value) {

    public RefreshTokenId {
        Objects.requireNonNull(value);
    }

    public static RefreshTokenId newId() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId of(String raw) {
        return new RefreshTokenId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
