package com.householdfinance.identity.domain;

/** Se lanza cuando se intenta operar sobre un usuario inactivo. */
public class InactiveUserException extends DomainException {
    public InactiveUserException(UserId userId) {
        super("El usuario " + userId + " no se encuentra activo");
    }
}
