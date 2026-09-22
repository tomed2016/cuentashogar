package com.householdfinance.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Identificador unico y publico de un usuario. */
public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "El identificador de usuario no puede ser nulo");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String raw) {
        Objects.requireNonNull(raw, "El identificador de usuario no puede ser nulo");
        return new UserId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
