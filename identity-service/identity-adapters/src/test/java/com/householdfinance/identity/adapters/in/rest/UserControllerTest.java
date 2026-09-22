package com.householdfinance.identity.adapters.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.householdfinance.identity.application.port.in.ChangePasswordUseCase;
import com.householdfinance.identity.application.port.in.GetCurrentUserUseCase;
import com.householdfinance.identity.application.port.in.UserView;
import com.householdfinance.identity.domain.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@Import(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @MockBean
    private ChangePasswordUseCase changePasswordUseCase;

    @Test
    @WithMockUser(username = "8f14e45f-ceea-467e-adc1-92aa96db8f7c")
    void meReturnsCurrentUser() throws Exception {
        UserId userId = UserId.of("8f14e45f-ceea-467e-adc1-92aa96db8f7c");
        when(getCurrentUserUseCase.getCurrentUser(any())).thenReturn(new UserView(
                userId.value().toString(), "ana@example.com", "Ana", "Gomez", "ACTIVE", Instant.now()));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    @WithMockUser(username = "8f14e45f-ceea-467e-adc1-92aa96db8f7c")
    void changePasswordReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"OldPass123\",\"newPassword\":\"NewPass456\"}"))
                .andExpect(status().isNoContent());
    }
}
