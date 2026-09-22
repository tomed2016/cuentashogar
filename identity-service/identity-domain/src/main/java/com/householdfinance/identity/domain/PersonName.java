package com.householdfinance.identity.domain;

import java.util.Objects;

/** Nombre y apellido del usuario. */
public record PersonName(String firstName, String lastName) {

    public PersonName {
        Objects.requireNonNull(firstName, "El nombre no puede ser nulo");
        Objects.requireNonNull(lastName, "El apellido no puede ser nulo");
        firstName = firstName.trim();
        lastName = lastName.trim();
        if (firstName.isEmpty() || firstName.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 1 y 100 caracteres");
        }
        if (lastName.isEmpty() || lastName.length() > 100) {
            throw new IllegalArgumentException("El apellido debe tener entre 1 y 100 caracteres");
        }
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
