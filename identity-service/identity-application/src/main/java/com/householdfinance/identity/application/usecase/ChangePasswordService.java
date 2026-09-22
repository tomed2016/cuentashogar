package com.householdfinance.identity.application.usecase;

import com.householdfinance.identity.application.port.in.ChangePasswordCommand;
import com.householdfinance.identity.application.port.in.ChangePasswordUseCase;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.domain.PasswordHasher;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.User;
import com.householdfinance.identity.domain.UserNotFoundException;

public final class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SystemClock clock;

    public ChangePasswordService(UserRepository userRepository, PasswordHasher passwordHasher, SystemClock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Override
    public void changePassword(ChangePasswordCommand command) {
        User user = userRepository
                .findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.changePassword(
                new RawPassword(command.currentRawPassword()),
                new RawPassword(command.newRawPassword()),
                passwordHasher,
                clock.now());

        userRepository.save(user);
    }
}
