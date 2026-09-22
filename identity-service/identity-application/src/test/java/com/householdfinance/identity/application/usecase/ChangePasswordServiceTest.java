package com.householdfinance.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.householdfinance.identity.application.port.in.ChangePasswordCommand;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.InvalidCredentialsException;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PasswordHasher;
import com.householdfinance.identity.domain.PersonName;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.User;
import com.householdfinance.identity.domain.UserId;
import com.householdfinance.identity.domain.UserNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    private static final String CURRENT_PASSWORD = "S3curePass!";
    private static final String NEW_PASSWORD = "N3wSecureP@ss";

    @Mock
    private UserRepository userRepository;

    private final PasswordHasher passwordHasher = new StaticPasswordHasher();
    private final SystemClock clock = () -> Instant.parse("2024-01-01T00:00:00Z");

    private ChangePasswordService service;

    @BeforeEach
    void setUp() {
        service = new ChangePasswordService(userRepository, passwordHasher, clock);
    }

    @Test
    void changesPasswordWhenCurrentPasswordMatches() {
        User user = User.register(
                new Email("member@example.com"),
                passwordHasher.hash(new RawPassword(CURRENT_PASSWORD)),
                new PersonName("Ada", "Lovelace"),
                clock.now());
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        service.changePassword(new ChangePasswordCommand(user.id(), CURRENT_PASSWORD, NEW_PASSWORD));

        user.authenticate(new RawPassword(NEW_PASSWORD), passwordHasher);
        assertThatThrownBy(() -> user.authenticate(new RawPassword(CURRENT_PASSWORD), passwordHasher))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(new ChangePasswordCommand(userId, CURRENT_PASSWORD, NEW_PASSWORD)))
                .isInstanceOf(UserNotFoundException.class);
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
