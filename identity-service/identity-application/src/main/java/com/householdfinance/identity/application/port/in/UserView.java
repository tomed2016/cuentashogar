package com.householdfinance.identity.application.port.in;

import java.time.Instant;

/** Vista de lectura de un usuario, independiente de la representacion de dominio o persistencia. */
public record UserView(
        String id, String email, String firstName, String lastName, String status, Instant createdAt) {}
