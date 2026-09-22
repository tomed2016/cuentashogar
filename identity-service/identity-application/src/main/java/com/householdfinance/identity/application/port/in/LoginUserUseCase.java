package com.householdfinance.identity.application.port.in;

public interface LoginUserUseCase {

    AuthResult login(LoginCommand command);
}
