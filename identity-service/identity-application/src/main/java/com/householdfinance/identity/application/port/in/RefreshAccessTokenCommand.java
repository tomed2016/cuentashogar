package com.householdfinance.identity.application.port.in;

/** Comando de entrada para renovar el access token usando un refresh token vigente. */
public record RefreshAccessTokenCommand(String refreshTokenId, String rawRefreshToken) {}
