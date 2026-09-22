package com.householdfinance.identity.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de la peticion de cambio de contrasena del usuario autenticado. */
public record ChangePasswordRequest(
        @NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 128) String newPassword) {}
