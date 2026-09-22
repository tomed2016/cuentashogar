package com.householdfinance.identity.adapters.in.rest.dto;

import com.householdfinance.identity.application.port.in.UserView;
import java.time.Instant;

/** Representacion publica de un usuario, sin datos sensibles. */
public record UserResponse(String id, String email, String firstName, String lastName, String status, Instant createdAt) {

    public static UserResponse from(UserView view) {
        return new UserResponse(view.id(), view.email(), view.firstName(), view.lastName(), view.status(), view.createdAt());
    }
}
