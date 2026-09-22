package com.householdfinance.identity.application.port.in;

import com.householdfinance.identity.domain.UserId;

/** Comando de entrada para cambiar la contrasena de un usuario autenticado. */
public record ChangePasswordCommand(UserId userId, String currentRawPassword, String newRawPassword) {}
