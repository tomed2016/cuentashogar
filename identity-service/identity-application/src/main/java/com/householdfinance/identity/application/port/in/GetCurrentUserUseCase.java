package com.householdfinance.identity.application.port.in;

import com.householdfinance.identity.domain.UserId;

public interface GetCurrentUserUseCase {

    UserView getCurrentUser(UserId userId);
}
