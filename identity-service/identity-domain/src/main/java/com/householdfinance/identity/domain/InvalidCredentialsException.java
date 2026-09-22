package com.householdfinance.identity.domain;

/**
 * Se lanza cuando las credenciales de inicio de sesion no son validas.
 * Deliberadamente no distingue entre "usuario inexistente" y "contrasena incorrecta"
 * para evitar la enumeracion de usuarios.
 */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("Credenciales invalidas");
    }
}
