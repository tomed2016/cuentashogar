package com.householdfinance.identity.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/** Direccion de correo electronico normalizada y validada del usuario. */
public record Email(String value) {

    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "El correo electronico no puede ser nulo");
        String normalized = value.trim().toLowerCase();
        if (!SIMPLE_EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException(value);
        }
        value = normalized;
    }

    public static final class InvalidEmailException extends DomainException {
        public InvalidEmailException(String rawValue) {
            super("El correo electronico '" + rawValue + "' no tiene un formato valido");
        }
    }
}
