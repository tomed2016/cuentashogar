package com.householdfinance.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.RefreshAccessTokenCommand;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.InvalidRefreshTokenException;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PersonName;
import com.householdfinance.identity.domain.RefreshToken;
import com.householdfinance.identity.domain.RefreshTokenId;
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
class RefreshAccessTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private RefreshTokenCrypto refreshTokenCrypto;

    private final SystemClock clock = () -> Instant.parse("2024-01-01T00:00:00Z");

    private RefreshAccessTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshAccessTokenService(
                userRepository, refreshTokenRepository, refreshTokenCrypto, clock, accessTokenIssuer, Duration.ofDays(30));
    }

    @Test
    void rotatesValidRefreshToken() {
        User user = User.register(
                new Email("member@example.com"),
                new PasswordHash("hashed:pw"),
                new PersonName("Ada", "Lovelace"),
                clock.now());
        RefreshToken existingToken =
                RefreshToken.issue(user.id(), "hashed-old-token", clock.now(), clock.now().plusSeconds(3600));

        when(refreshTokenRepository.findById(existingToken.id())).thenReturn(Optional.of(existingToken));
        when(refreshTokenCrypto.matches("raw-old-token", "hashed-old-token")).thenReturn(true);
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(accessTokenIssuer.issue(any(), any(), any()))
                .thenReturn(new AccessTokenIssuer.IssuedAccessToken("new-access-token", clock.now().plusSeconds(900)));
        when(refreshTokenCrypto.generateRawToken()).thenReturn("raw-new-token");
        when(refreshTokenCrypto.hash("raw-new-token")).thenReturn("hashed-new-token");

        AuthResult result = service.refresh(
                new RefreshAccessTokenCommand(existingToken.id().toString(), "raw-old-token"));

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(existingToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(existingToken);
    }

    @Test
    void rejectsUnknownRefreshToken() {
        RefreshTokenId id = RefreshTokenId.newId();
        when(refreshTokenRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshAccessTokenCommand(id.toString(), "raw-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsExpiredRefreshToken() {
        User user = User.register(
                new Email("member@example.com"),
                new PasswordHash("hashed:pw"),
                new PersonName("Ada", "Lovelace"),
                clock.now());
        RefreshToken expiredToken = RefreshToken.issue(
                user.id(), "hashed-old-token", clock.now().minusSeconds(7200), clock.now().minusSeconds(3600));
        when(refreshTokenRepository.findById(expiredToken.id())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> service.refresh(
                        new RefreshAccessTokenCommand(expiredToken.id().toString(), "raw-old-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
