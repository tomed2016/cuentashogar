package com.householdfinance.identity.domain;

import java.util.Objects;

/**
 * Representa el hash seguro de una contrasena. El dominio nunca conoce ni manipula
 * contrasenas en texto plano; solo transporta el resultado ya calculado por un
 * {@link PasswordHasher} de infraestructura.
 */
public record PasswordHash(String value) {

    public PasswordHash {
        Objects.requireNonNull(value, "El hash de contrasena no puede ser nulo");
        if (value.isBlank()) {
            throw new IllegalArgumentException("El hash de contrasena no puede estar vacio");
        }
    }
}
