package com.householdfinance.identity.domain;

/** Se lanza cuando un refresh token es invalido, expirado o ya fue revocado/rotado. */
public class InvalidRefreshTokenException extends DomainException {
    public InvalidRefreshTokenException() {
        super("El refresh token es invalido, expiro o ya fue utilizado");
    }
}
