package com.householdfinance.identity.application.port.in;

/** Comando de entrada para iniciar sesion con credenciales. */
public record LoginCommand(String email, String rawPassword) {}
