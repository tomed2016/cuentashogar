package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.LogoutCommand;
import com.householdfinance.identity.application.port.in.LogoutUserUseCase;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.domain.RefreshToken;
import com.householdfinance.identity.domain.RefreshTokenId;

public final class LogoutUserService implements LogoutUserUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUserService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void logout(LogoutCommand command) {
        RefreshTokenId id = RefreshTokenId.of(command.refreshTokenId());
        refreshTokenRepository.findById(id).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }
}
