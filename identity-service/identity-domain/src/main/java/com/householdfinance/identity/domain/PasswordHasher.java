package com.householdfinance.identity.domain;

/**
 * Puerto de dominio para el hashing y verificacion segura de contrasenas.
 * La implementacion concreta (BCrypt, Argon2, etc.) vive en la capa de adaptadores.
 */
public interface PasswordHasher {

    PasswordHash hash(RawPassword rawPassword);

    boolean matches(RawPassword rawPassword, PasswordHash hash);
}
