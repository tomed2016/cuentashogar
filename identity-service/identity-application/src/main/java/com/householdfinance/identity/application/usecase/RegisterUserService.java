package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.RegisterUserCommand;
import com.householdfinance.identity.application.port.in.RegisterUserUseCase;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PasswordHasher;
import com.householdfinance.identity.domain.PersonName;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.User;
import com.householdfinance.identity.domain.UserAlreadyExistsException;
import java.time.Duration;
import java.time.Instant;

public final class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SystemClock clock;
    private final AuthTokenFactory authTokenFactory;

    public RegisterUserService(
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
    public AuthResult register(RegisterUserCommand command) {
        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        Instant now = clock.now();
        PasswordHash passwordHash = passwordHasher.hash(new RawPassword(command.rawPassword()));
        User user = User.register(email, passwordHash, new PersonName(command.firstName(), command.lastName()), now);
        userRepository.save(user);

        return authTokenFactory.issueTokens(user.id(), user.email(), now);
    }
}
