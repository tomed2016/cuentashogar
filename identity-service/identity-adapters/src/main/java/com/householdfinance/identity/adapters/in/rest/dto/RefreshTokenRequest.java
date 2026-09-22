package com.householdfinance.identity.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de la peticion de renovacion de un access token mediante un refresh token vigente. */
public record RefreshTokenRequest(@NotBlank String refreshTokenId, @NotBlank String refreshToken) {}
