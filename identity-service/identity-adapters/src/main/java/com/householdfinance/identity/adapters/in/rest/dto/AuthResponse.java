package com.householdfinance.identity.adapters.in.rest.dto;

import com.householdfinance.identity.application.port.in.AuthResult;
import java.time.Instant;

/** Respuesta con el resultado de una operacion de autenticacion exitosa. */
public record AuthResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshTokenId,
        String refreshToken,
        Instant refreshTokenExpiresAt) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.accessToken(),
                result.accessTokenExpiresAt(),
                result.refreshTokenId(),
                result.refreshToken(),
                result.refreshTokenExpiresAt());
    }
}
