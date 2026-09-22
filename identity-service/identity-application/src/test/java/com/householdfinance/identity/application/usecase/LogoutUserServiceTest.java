package com.householdfinance.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.householdfinance.identity.application.port.in.LogoutCommand;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PersonName;
import com.householdfinance.identity.domain.RefreshToken;
import com.householdfinance.identity.domain.RefreshTokenId;
import com.householdfinance.identity.domain.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutUserServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private LogoutUserService service;

    @BeforeEach
    void setUp() {
        service = new LogoutUserService(refreshTokenRepository);
    }

    @Test
    void revokesExistingRefreshToken() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        User user = User.register(new Email("member@example.com"), new PasswordHash("hashed:pw"),
                new PersonName("Ada", "Lovelace"), now);
        RefreshToken token = RefreshToken.issue(user.id(), "hashed-token", now, now.plusSeconds(3600));
        when(refreshTokenRepository.findById(token.id())).thenReturn(Optional.of(token));

        service.logout(new LogoutCommand(token.id().toString()));

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void isSilentWhenRefreshTokenDoesNotExist() {
        RefreshTokenId id = RefreshTokenId.newId();
        when(refreshTokenRepository.findById(id)).thenReturn(Optional.empty());

        service.logout(new LogoutCommand(id.toString()));

        verify(refreshTokenRepository, never()).save(any());
    }
}
