package com.householdfinance.identity.adapters.in.rest;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.householdfinance.identity.application.port.in.AuthResult;
import com.householdfinance.identity.application.port.in.LoginCommand;
import com.householdfinance.identity.application.port.in.LoginUserUseCase;
import com.householdfinance.identity.application.port.in.LogoutUserUseCase;
import com.householdfinance.identity.application.port.in.RefreshAccessTokenUseCase;
import com.householdfinance.identity.application.port.in.RegisterUserCommand;
import com.householdfinance.identity.application.port.in.RegisterUserUseCase;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @MockBean
    private LogoutUserUseCase logoutUserUseCase;

    @Test
    void registerReturnsCreatedWithTokens() throws Exception {
        var result = new AuthResult("access-token", Instant.now().plusSeconds(900), "refresh-id", "refresh-token",
                Instant.now().plusSeconds(2_592_000));
        when(registerUserUseCase.register(any(RegisterUserCommand.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new java.util.HashMap<>() {
                                    {
                                        put("email", "ana@example.com");
                                        put("password", "SecurePass123");
                                        put("firstName", "Ana");
                                        put("lastName", "Gomez");
                                    }
                                })))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("access-token")));
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"123\",\"firstName\":\"\",\"lastName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokens() throws Exception {
        var result = new AuthResult("access-token", Instant.now().plusSeconds(900), "refresh-id", "refresh-token",
                Instant.now().plusSeconds(2_592_000));
        when(loginUserUseCase.login(any(LoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"SecurePass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshTokenId", is("refresh-id")));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshTokenId\":\"refresh-id\"}"))
                .andExpect(status().isNoContent());
        verify(logoutUserUseCase).logout(any());
    }
}
