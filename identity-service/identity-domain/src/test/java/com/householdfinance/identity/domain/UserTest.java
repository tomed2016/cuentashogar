package com.householdfinance.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final PasswordHasher IN_MEMORY_HASHER = new PasswordHasher() {
        @Override
        public PasswordHash hash(RawPassword rawPassword) {
            return new PasswordHash("hashed:" + rawPassword.value());
        }

        @Override
        public boolean matches(RawPassword rawPassword, PasswordHash hash) {
            return hash.value().equals("hashed:" + rawPassword.value());
        }
    };

    @Test
    void registersActiveUserByDefault() {
        User user = User.register(
                new Email("john@example.com"),
                IN_MEMORY_HASHER.hash(new RawPassword("SecurePass1")),
                new PersonName("John", "Doe"),
                Instant.now());

        assertThat(user.isActive()).isTrue();
        assertThat(user.email().value()).isEqualTo("john@example.com");
    }

    @Test
    void authenticateFailsWithWrongPassword() {
        User user = User.register(
                new Email("john@example.com"),
                IN_MEMORY_HASHER.hash(new RawPassword("SecurePass1")),
                new PersonName("John", "Doe"),
                Instant.now());

        assertThatThrownBy(() -> user.authenticate(new RawPassword("WrongPass1"), IN_MEMORY_HASHER))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void authenticateFailsWhenUserInactive() {
        User user = User.register(
                new Email("john@example.com"),
                IN_MEMORY_HASHER.hash(new RawPassword("SecurePass1")),
                new PersonName("John", "Doe"),
                Instant.now());
        user.deactivate(Instant.now());

        assertThatThrownBy(() -> user.authenticate(new RawPassword("SecurePass1"), IN_MEMORY_HASHER))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void changePasswordRequiresCurrentPasswordToMatch() {
        User user = User.register(
                new Email("john@example.com"),
                IN_MEMORY_HASHER.hash(new RawPassword("SecurePass1")),
                new PersonName("John", "Doe"),
                Instant.now());

        assertThatThrownBy(() -> user.changePassword(
                        new RawPassword("WrongPass1"),
                        new RawPassword("NewSecurePass1"),
                        IN_MEMORY_HASHER,
                        Instant.now()))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void changePasswordUpdatesHashWhenCurrentMatches() {
        User user = User.register(
                new Email("john@example.com"),
                IN_MEMORY_HASHER.hash(new RawPassword("SecurePass1")),
                new PersonName("John", "Doe"),
                Instant.now());

        user.changePassword(
                new RawPassword("SecurePass1"), new RawPassword("NewSecurePass1"), IN_MEMORY_HASHER, Instant.now());

        assertThat(IN_MEMORY_HASHER.matches(new RawPassword("NewSecurePass1"), user.passwordHash()))
                .isTrue();
    }

    @Test
    void rejectsWeakPassword() {
        assertThatThrownBy(() -> new RawPassword("weak")).isInstanceOf(RawPassword.WeakPasswordException.class);
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> new Email("not-an-email")).isInstanceOf(Email.InvalidEmailException.class);
    }
}
