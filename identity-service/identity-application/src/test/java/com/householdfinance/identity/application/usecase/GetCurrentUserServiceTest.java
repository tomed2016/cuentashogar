package com.householdfinance.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.householdfinance.identity.application.port.in.UserView;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.PasswordHash;
import com.householdfinance.identity.domain.PersonName;
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
class GetCurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private GetCurrentUserService service;

    @BeforeEach
    void setUp() {
        service = new GetCurrentUserService(userRepository);
    }

    @Test
    void returnsViewOfExistingUser() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        User user = User.register(
                new Email("member@example.com"), new PasswordHash("hashed:pw"), new PersonName("Ada", "Lovelace"), now);
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        UserView view = service.getCurrentUser(user.id());

        assertThat(view.email()).isEqualTo("member@example.com");
        assertThat(view.firstName()).isEqualTo("Ada");
        assertThat(view.status()).isEqualTo("ACTIVE");
    }

    @Test
    void throwsWhenUserNotFound() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser(userId)).isInstanceOf(UserNotFoundException.class);
    }
}
