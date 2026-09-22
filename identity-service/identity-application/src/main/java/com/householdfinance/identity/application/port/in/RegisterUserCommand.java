package com.householdfinance.identity.application.port.in;

/** Comando de entrada para registrar un nuevo usuario. */
public record RegisterUserCommand(String email, String rawPassword, String firstName, String lastName) {}
