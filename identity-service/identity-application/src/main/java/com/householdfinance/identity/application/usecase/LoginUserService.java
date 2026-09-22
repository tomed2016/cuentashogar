package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.LoginCommand;
import com.householdfinance.identity.application.port.in.LoginUserUseCase;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.InvalidCredentialsException;
import com.householdfinance.identity.domain.PasswordHasher;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.User;
import java.time.Duration;
import java.time.Instant;

public final class LoginUserService implements LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SystemClock clock;
    private final AuthTokenFactory authTokenFactory;

    public LoginUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            SystemClock clock,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenCrypto refreshTokenCrypto,
            Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
        this.authTokenFactory =
                new AuthTokenFactory(accessTokenIssuer, refreshTokenRepository, refreshTokenCrypto, refreshTokenTtl);
    }

    @Override
    public AuthResult login(LoginCommand command) {
        Email email = new Email(command.email());
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        user.authenticate(new RawPassword(command.rawPassword()), passwordHasher);

        return authTokenFactory.issueTokens(user.id(), user.email(), clock.now());
    }
}
