package com.householdfinance.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.RegisterUserCommand;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PasswordHasher;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.User;
import com.householdfinance.identity.domain.UserAlreadyExistsException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private RefreshTokenCrypto refreshTokenCrypto;

    private final PasswordHasher passwordHasher = new InMemoryPasswordHasher();
    private final SystemClock clock = () -> Instant.parse("2024-01-01T00:00:00Z");

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(
                userRepository,
                passwordHasher,
                clock,
                accessTokenIssuer,
                refreshTokenRepository,
                refreshTokenCrypto,
                Duration.ofDays(30));
    }

    @Test
    void registersNewUserAndIssuesTokens() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(accessTokenIssuer.issue(any(), any(), any()))
                .thenReturn(new AccessTokenIssuer.IssuedAccessToken("access-token", clock.now().plusSeconds(900)));
        when(refreshTokenCrypto.generateRawToken()).thenReturn("raw-refresh-token");
        when(refreshTokenCrypto.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");

        RegisterUserCommand command = new RegisterUserCommand("new@example.com", "S3curePass!", "Ada", "Lovelace");

        AuthResult result = service.register(command);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh-token");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void rejectsRegistrationWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        RegisterUserCommand command =
                new RegisterUserCommand("existing@example.com", "S3curePass!", "Ada", "Lovelace");

        assertThatThrownBy(() -> service.register(command)).isInstanceOf(UserAlreadyExistsException.class);
    }

    /** Password hasher de prueba: no realiza hashing real, solo antepone un marcador. */
    private static final class InMemoryPasswordHasher implements PasswordHasher {
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
