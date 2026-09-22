package com.householdfinance.identity.domain;

/** Se lanza al intentar registrar un usuario con un correo ya existente. */
public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(Email email) {
        super("Ya existe un usuario registrado con el correo indicado");
    }
}
