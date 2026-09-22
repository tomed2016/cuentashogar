package com.householdfinance.identity.adapters.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Cuerpo de la peticion de inicio de sesion. */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
