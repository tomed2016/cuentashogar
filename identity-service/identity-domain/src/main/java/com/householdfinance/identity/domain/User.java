package com.householdfinance.identity.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Agregado raiz que representa la identidad y credenciales de un usuario de la plataforma.
 * No contiene ninguna anotacion de framework: es un objeto Java puro.
 */
public final class User {

    private final UserId id;
    private Email email;
    private PasswordHash passwordHash;
    private PersonName name;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UserId id,
            Email email,
            PasswordHash passwordHash,
            PersonName name,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /** Registra un nuevo usuario activo con la contrasena ya hasheada. */
    public static User register(Email email, PasswordHash passwordHash, PersonName name, Instant now) {
        return new User(UserId.newId(), email, passwordHash, name, UserStatus.ACTIVE, now, now);
    }

    /** Reconstruye un usuario existente a partir de datos persistidos. */
    public static User rehydrate(
            UserId id,
            Email email,
            PasswordHash passwordHash,
            PersonName name,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new User(id, email, passwordHash, name, status, createdAt, updatedAt);
    }

    public void activate(Instant now) {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void deactivate(Instant now) {
        this.status = UserStatus.INACTIVE;
        this.updatedAt = now;
    }

    /**
     * Cambia la contrasena del usuario verificando primero la contrasena actual mediante el
     * {@link PasswordHasher} provisto.
     */
    public void changePassword(
            RawPassword currentRawPassword, RawPassword newRawPassword, PasswordHasher hasher, Instant now) {
        requireActive();
        if (!hasher.matches(currentRawPassword, this.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        this.passwordHash = hasher.hash(newRawPassword);
        this.updatedAt = now;
    }

    /** Verifica credenciales de autenticacion. Lanza excepcion de dominio si son invalidas. */
    public void authenticate(RawPassword rawPassword, PasswordHasher hasher) {
        if (this.status != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }
        if (!hasher.matches(rawPassword, this.passwordHash)) {
            throw new InvalidCredentialsException();
        }
    }

    private void requireActive() {
        if (this.status != UserStatus.ACTIVE) {
            throw new InactiveUserException(this.id);
        }
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public PersonName name() {
        return name;
    }

    public UserStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
