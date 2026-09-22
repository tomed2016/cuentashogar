package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.RefreshAccessTokenCommand;
import com.householdfinance.identity.application.port.in.RefreshAccessTokenUseCase;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.InvalidRefreshTokenException;
import com.householdfinance.identity.domain.RefreshToken;
import com.householdfinance.identity.domain.RefreshTokenId;
import com.householdfinance.identity.domain.User;
import java.time.Duration;
import java.time.Instant;

/**
 * Renueva un access token utilizando un refresh token vigente, aplicando rotacion: el refresh
 * token presentado se revoca y se emite uno nuevo.
 */
public final class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCrypto refreshTokenCrypto;
    private final SystemClock clock;
    private final AuthTokenFactory authTokenFactory;

    public RefreshAccessTokenService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenCrypto refreshTokenCrypto,
            SystemClock clock,
            AccessTokenIssuer accessTokenIssuer,
            Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenCrypto = refreshTokenCrypto;
        this.clock = clock;
        this.authTokenFactory =
                new AuthTokenFactory(accessTokenIssuer, refreshTokenRepository, refreshTokenCrypto, refreshTokenTtl);
    }

    @Override
    public AuthResult refresh(RefreshAccessTokenCommand command) {
        RefreshTokenId id = RefreshTokenId.of(command.refreshTokenId());
        RefreshToken currentToken =
                refreshTokenRepository.findById(id).orElseThrow(InvalidRefreshTokenException::new);

        Instant now = clock.now();
        if (!currentToken.isValid(now) || !refreshTokenCrypto.matches(command.rawRefreshToken(), currentToken.tokenHash())) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(currentToken.userId()).orElseThrow(InvalidRefreshTokenException::new);
        if (!user.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        AuthResult result = authTokenFactory.issueTokens(user.id(), user.email(), now);
        currentToken.rotate(RefreshTokenId.of(result.refreshTokenId()));
        refreshTokenRepository.save(currentToken);

        return result;
    }
}
