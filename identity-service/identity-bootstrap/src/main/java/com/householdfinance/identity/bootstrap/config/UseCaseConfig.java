package com.householdfinance.identity.bootstrap.config;

import com.householdfinance.identity.adapters.out.security.JwtProperties;
import com.householdfinance.identity.application.port.out.AccessTokenIssuer;
import com.householdfinance.identity.application.port.out.RefreshTokenCrypto;
import com.householdfinance.identity.application.port.out.RefreshTokenRepository;
import com.householdfinance.identity.application.port.out.SystemClock;
import com.householdfinance.identity.application.port.out.UserRepository;
import com.householdfinance.identity.application.usecase.ChangePasswordService;
import com.householdfinance.identity.application.usecase.GetCurrentUserService;
import com.householdfinance.identity.application.usecase.LoginUserService;
import com.householdfinance.identity.application.usecase.LogoutUserService;
import com.householdfinance.identity.application.usecase.RefreshAccessTokenService;
import com.householdfinance.identity.application.usecase.RegisterUserService;
import com.householdfinance.identity.domain.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los casos de uso de la capa de aplicacion como beans de Spring. La capa de aplicacion
 * permanece libre de anotaciones de framework; este ensamblaje ocurre unicamente en el modulo de
 * bootstrap, respetando la inversion de dependencias de la arquitectura hexagonal.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public SystemClock systemClock() {
        return SystemClock.system();
    }

    @Bean
    public RegisterUserService registerUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            SystemClock clock,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenCrypto refreshTokenCrypto,
            JwtProperties jwtProperties) {
        return new RegisterUserService(
                userRepository,
                passwordHasher,
                clock,
                accessTokenIssuer,
                refreshTokenRepository,
                refreshTokenCrypto,
                jwtProperties.refreshTokenTtl());
    }

    @Bean
    public LoginUserService loginUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            SystemClock clock,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenCrypto refreshTokenCrypto,
            JwtProperties jwtProperties) {
        return new LoginUserService(
                userRepository,
                passwordHasher,
                clock,
                accessTokenIssuer,
                refreshTokenRepository,
                refreshTokenCrypto,
                jwtProperties.refreshTokenTtl());
    }

    @Bean
    public RefreshAccessTokenService refreshAccessTokenService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenCrypto refreshTokenCrypto,
            SystemClock clock,
            AccessTokenIssuer accessTokenIssuer,
            JwtProperties jwtProperties) {
        return new RefreshAccessTokenService(
                userRepository,
                refreshTokenRepository,
                refreshTokenCrypto,
                clock,
                accessTokenIssuer,
                jwtProperties.refreshTokenTtl());
    }

    @Bean
    public LogoutUserService logoutUserService(RefreshTokenRepository refreshTokenRepository) {
        return new LogoutUserService(refreshTokenRepository);
    }

    @Bean
    public ChangePasswordService changePasswordService(
            UserRepository userRepository, PasswordHasher passwordHasher, SystemClock clock) {
        return new ChangePasswordService(userRepository, passwordHasher, clock);
    }

    @Bean
    public GetCurrentUserService getCurrentUserService(UserRepository userRepository) {
        return new GetCurrentUserService(userRepository);
    }
}
