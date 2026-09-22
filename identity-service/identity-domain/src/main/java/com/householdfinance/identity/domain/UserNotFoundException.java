package com.householdfinance.identity.domain;

/** Se lanza cuando se busca un usuario por identificador y no existe. */
public final class UserNotFoundException extends DomainException {

    public UserNotFoundException(UserId id) {
        super("Usuario no encontrado: " + id);
    }
}
