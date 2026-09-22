package com.householdfinance.identity.adapters.in.rest;

import com.householdfinance.identity.adapters.in.rest.dto.AuthResponse;
import com.householdfinance.identity.adapters.in.rest.dto.LoginRequest;
import com.householdfinance.identity.adapters.in.rest.dto.LogoutRequest;
import com.householdfinance.identity.adapters.in.rest.dto.RefreshTokenRequest;
import com.householdfinance.identity.adapters.in.rest.dto.RegisterUserRequest;
import com.householdfinance.identity.application.port.in.LoginCommand;
import com.householdfinance.identity.application.port.in.LoginUserUseCase;
import com.householdfinance.identity.application.port.in.LogoutCommand;
import com.householdfinance.identity.application.port.in.LogoutUserUseCase;
import com.householdfinance.identity.application.port.in.RefreshAccessTokenCommand;
import com.householdfinance.identity.application.port.in.RefreshAccessTokenUseCase;
import com.householdfinance.identity.application.port.in.RegisterUserCommand;
import com.householdfinance.identity.application.port.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para el ciclo de vida de autenticacion de usuarios. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro, inicio de sesion y gestion de sesiones")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUserUseCase logoutUserUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            RefreshAccessTokenUseCase refreshAccessTokenUseCase,
            LogoutUserUseCase logoutUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un nuevo usuario y emite tokens iniciales")
    public AuthResponse register(@Valid @RequestBody RegisterUserRequest request) {
        var result = registerUserUseCase.register(
                new RegisterUserCommand(request.email(), request.password(), request.firstName(), request.lastName()));
        return AuthResponse.from(result);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica un usuario mediante correo y contrasena")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var result = loginUserUseCase.login(new LoginCommand(request.email(), request.password()));
        return AuthResponse.from(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva el access token rotando el refresh token vigente")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = refreshAccessTokenUseCase.refresh(
                new RefreshAccessTokenCommand(request.refreshTokenId(), request.refreshToken()));
        return AuthResponse.from(result);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoca el refresh token indicado, cerrando la sesion")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUserUseCase.logout(new LogoutCommand(request.refreshTokenId()));
        return ResponseEntity.noContent().build();
    }
}
