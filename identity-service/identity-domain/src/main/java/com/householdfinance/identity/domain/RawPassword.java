package com.householdfinance.identity.domain;

import java.util.Objects;

/**
 * Contrasena en texto plano recibida del exterior. Vive unicamente en memoria durante
 * el procesamiento de un comando y nunca debe persistirse, registrarse en logs ni serializarse.
 */
public record RawPassword(String value) {

    private static final int MIN_LENGTH = 8;

    public RawPassword {
        Objects.requireNonNull(value, "La contrasena no puede ser nula");
        if (value.length() < MIN_LENGTH) {
            throw new WeakPasswordException(MIN_LENGTH);
        }
    }

    @Override
    public String toString() {
        return "RawPassword[REDACTED]";
    }

    public static final class WeakPasswordException extends DomainException {
        public WeakPasswordException(int minLength) {
            super("La contrasena debe tener al menos " + minLength + " caracteres");
        }
    }
}
