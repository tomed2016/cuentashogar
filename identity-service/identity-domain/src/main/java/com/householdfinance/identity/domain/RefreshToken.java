package com.householdfinance.identity.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Representa una sesion de refresh token con soporte de rotacion. Solo se persiste un hash
 * del token, nunca el valor en texto plano.
 */
public final class RefreshToken {

    private final RefreshTokenId id;
    private final UserId userId;
    private final String tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private boolean revoked;
    private RefreshTokenId replacedBy;

    private RefreshToken(
            RefreshTokenId id,
            UserId userId,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt,
            boolean revoked,
            RefreshTokenId replacedBy) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revoked = revoked;
        this.replacedBy = replacedBy;
    }

    public static RefreshToken issue(UserId userId, String tokenHash, Instant issuedAt, Instant expiresAt) {
        return new RefreshToken(RefreshTokenId.newId(), userId, tokenHash, issuedAt, expiresAt, false, null);
    }

    public static RefreshToken rehydrate(
            RefreshTokenId id,
            UserId userId,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt,
            boolean revoked,
            RefreshTokenId replacedBy) {
        return new RefreshToken(id, userId, tokenHash, issuedAt, expiresAt, revoked, replacedBy);
    }

    public boolean isValid(Instant now) {
        return !revoked && now.isBefore(expiresAt);
    }

    /** Marca este token como revocado, registrando el token que lo reemplaza (rotacion). */
    public void rotate(RefreshTokenId newTokenId) {
        if (revoked) {
            throw new InvalidRefreshTokenException();
        }
        this.revoked = true;
        this.replacedBy = newTokenId;
    }

    public void revoke() {
        this.revoked = true;
    }

    public RefreshTokenId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public RefreshTokenId replacedBy() {
        return replacedBy;
    }
}
