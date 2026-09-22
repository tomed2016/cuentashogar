package com.householdfinance.identity.adapters.in.rest;

import com.householdfinance.identity.adapters.in.rest.dto.ChangePasswordRequest;
import com.householdfinance.identity.adapters.in.rest.dto.UserResponse;
import com.householdfinance.identity.application.port.in.ChangePasswordCommand;
import com.householdfinance.identity.application.port.in.ChangePasswordUseCase;
import com.householdfinance.identity.application.port.in.GetCurrentUserUseCase;
import com.householdfinance.identity.domain.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para operaciones sobre el usuario autenticado. */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Operaciones sobre el usuario autenticado")
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public UserController(GetCurrentUserUseCase getCurrentUserUseCase, ChangePasswordUseCase changePasswordUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtiene los datos del usuario autenticado")
    public UserResponse me(Authentication authentication) {
        var view = getCurrentUserUseCase.getCurrentUser(currentUserId(authentication));
        return UserResponse.from(view);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambia la contrasena del usuario autenticado")
    public ResponseEntity<Void> changePassword(
            Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.changePassword(
                new ChangePasswordCommand(currentUserId(authentication), request.currentPassword(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    private UserId currentUserId(Authentication authentication) {
        return UserId.of(authentication.getName());
    }
}
