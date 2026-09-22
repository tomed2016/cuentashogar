package com.householdfinance.identity.application.port.in;

public interface RefreshAccessTokenUseCase {

    AuthResult refresh(RefreshAccessTokenCommand command);
}
