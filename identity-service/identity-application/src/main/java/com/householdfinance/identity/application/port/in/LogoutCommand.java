package com.householdfinance.identity.application.port.in;

/** Comando de entrada para cerrar sesion revocando un refresh token especifico. */
public record LogoutCommand(String refreshTokenId) {}
