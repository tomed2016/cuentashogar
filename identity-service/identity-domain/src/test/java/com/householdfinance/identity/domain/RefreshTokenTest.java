package com.householdfinance.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    void isValidWhenNotRevokedAndNotExpired() {
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.issue(UserId.newId(), "hash", now, now.plus(30, ChronoUnit.DAYS));

        assertThat(token.isValid(now.plus(1, ChronoUnit.DAYS))).isTrue();
    }

    @Test
    void isInvalidWhenExpired() {
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.issue(UserId.newId(), "hash", now, now.plus(1, ChronoUnit.DAYS));

        assertThat(token.isValid(now.plus(2, ChronoUnit.DAYS))).isFalse();
    }

    @Test
    void rotateMarksTokenAsRevokedAndLinksReplacement() {
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.issue(UserId.newId(), "hash", now, now.plus(30, ChronoUnit.DAYS));
        RefreshTokenId newId = RefreshTokenId.newId();

        token.rotate(newId);

        assertThat(token.isRevoked()).isTrue();
        assertThat(token.replacedBy()).isEqualTo(newId);
        assertThat(token.isValid(now)).isFalse();
    }

    @Test
    void rotatingAnAlreadyRevokedTokenFails() {
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.issue(UserId.newId(), "hash", now, now.plus(30, ChronoUnit.DAYS));
        token.revoke();

        assertThatThrownBy(() -> token.rotate(RefreshTokenId.newId()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
