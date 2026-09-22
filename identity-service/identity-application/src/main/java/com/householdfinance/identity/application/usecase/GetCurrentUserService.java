package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.GetCurrentUserUseCase;
import com.householdfinance.identity.application.port.in.UserView;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.User;
import com.householdfinance.identity.domain.UserId;
import com.householdfinance.identity.domain.UserNotFoundException;

public final class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserView getCurrentUser(UserId userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return toView(user);
    }

    private static UserView toView(User user) {
        return new UserView(
                user.id().toString(),
                user.email().value(),
                user.name().firstName(),
                user.name().lastName(),
                user.status().name(),
                user.createdAt());
    }
}
