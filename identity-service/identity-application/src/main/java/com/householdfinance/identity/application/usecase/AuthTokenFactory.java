package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.RefreshToken;
import com.householdfinance.identity.domain.UserId;
import java.time.Duration;
import java.time.Instant;

/**
 * Colabora entre los distintos casos de uso de autenticacion para emitir de forma consistente
 * el par access token / refresh token. No es un caso de uso publico, solo un ensamblador interno
 * de la capa de aplicacion.
 */
final class AuthTokenFactory {

    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCrypto refreshTokenCrypto;
    private final Duration refreshTokenTtl;

    AuthTokenFactory(
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenCrypto refreshTokenCrypto,
            Duration refreshTokenTtl) {
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenCrypto = refreshTokenCrypto;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    AuthResult issueTokens(UserId userId, Email email, Instant now) {
        AccessTokenIssuer.IssuedAccessToken accessToken = accessTokenIssuer.issue(userId, email, now);

        String rawRefreshToken = refreshTokenCrypto.generateRawToken();
        Instant refreshExpiresAt = now.plus(refreshTokenTtl);
        RefreshToken refreshToken = RefreshToken.issue(
                userId, refreshTokenCrypto.hash(rawRefreshToken), now, refreshExpiresAt);
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(
                accessToken.token(),
                accessToken.expiresAt(),
                refreshToken.id().toString(),
                rawRefreshToken,
                refreshExpiresAt);
    }
}
