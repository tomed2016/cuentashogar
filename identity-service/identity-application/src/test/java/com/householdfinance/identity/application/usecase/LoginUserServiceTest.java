package com.householdfinance.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.LoginCommand;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.InvalidCredentialsException;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PasswordHasher;
import com.householdfinance.identity.domain.PersonName;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUserServiceTest {

    private static final String RAW_PASSWORD = "S3curePass!";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private RefreshTokenCrypto refreshTokenCrypto;

    private final PasswordHasher passwordHasher = new StaticPasswordHasher();
    private final SystemClock clock = () -> Instant.parse("2024-01-01T00:00:00Z");

    private LoginUserService service;

    @BeforeEach
    void setUp() {
        service = new LoginUserService(
                userRepository,
                passwordHasher,
                clock,
                accessTokenIssuer,
                refreshTokenRepository,
                refreshTokenCrypto,
                Duration.ofDays(30));
    }

    @Test
    void logsInWithValidCredentials() {
        User user = User.register(
                new Email("member@example.com"),
                passwordHasher.hash(new RawPassword(RAW_PASSWORD)),
                new PersonName("Ada", "Lovelace"),
                clock.now());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(accessTokenIssuer.issue(any(), any(), any()))
                .thenReturn(new AccessTokenIssuer.IssuedAccessToken("access-token", clock.now().plusSeconds(900)));
        when(refreshTokenCrypto.generateRawToken()).thenReturn("raw-refresh-token");
        when(refreshTokenCrypto.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");

        AuthResult result = service.login(new LoginCommand("member@example.com", RAW_PASSWORD));

        assertThat(result.accessToken()).isEqualTo("access-token");
    }

    @Test
    void rejectsLoginWhenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("missing@example.com", RAW_PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsLoginWhenPasswordDoesNotMatch() {
        User user = User.register(
                new Email("member@example.com"),
                passwordHasher.hash(new RawPassword(RAW_PASSWORD)),
                new PersonName("Ada", "Lovelace"),
                clock.now());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginCommand("member@example.com", "WrongPass1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private static final class StaticPasswordHasher implements PasswordHasher {
        @Override
        public PasswordHash hash(RawPassword rawPassword) {
            return new PasswordHash("hashed:" + rawPassword.value());
        }

        @Override
        public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
            return passwordHash.value().equals("hashed:" + rawPassword.value());
        }
    }
}
