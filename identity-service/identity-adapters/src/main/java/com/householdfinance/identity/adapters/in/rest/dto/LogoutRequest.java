package com.householdfinance.identity.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de la peticion de cierre de sesion, revocando el refresh token indicado. */
public record LogoutRequest(@NotBlank String refreshTokenId) {}
