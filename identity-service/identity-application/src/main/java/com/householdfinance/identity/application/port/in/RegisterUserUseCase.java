package com.householdfinance.identity.application.port.in;

public interface RegisterUserUseCase {

    AuthResult register(RegisterUserCommand command);
}
