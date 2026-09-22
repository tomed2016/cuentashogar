package com.householdfinance.identity.domain;

/** Excepcion base para todas las violaciones de reglas del dominio de identidad. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
