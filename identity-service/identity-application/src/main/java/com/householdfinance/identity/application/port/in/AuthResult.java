package com.householdfinance.identity.application.port.in;

import java.time.Instant;

/** Resultado de una operacion de autenticacion exitosa: par de tokens emitidos. */
public record AuthResult(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshTokenId,
        String refreshToken,
        Instant refreshTokenExpiresAt) {}
