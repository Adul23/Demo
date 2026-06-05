package com.example.demo;

import com.example.demo.controllers.AuthenticationController;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.VerifyDto;
import com.example.demo.models.User;
import com.example.demo.responses.LoginResponse;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Для Spring Boot 3.4+. Если версия ниже, используй org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности (JWT фильтр), чтобы они не мешали тестировать чистую логику контроллера
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Поможет превращать DTO в JSON строки

    @MockitoBean // Если импорт не проходит, замени на @MockBean
    private JwtService jwtService;

    @MockitoBean // Если импорт не проходит, замени на @MockBean
    private AuthenticationService authenticationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        // Заполни здесь другие поля mockUser, если необходимо
    }

    @Test
    void register_ShouldReturnUser_WhenValidRequest() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        when(authenticationService.signUp(any(RegisterDto.class))).thenReturn(mockUser);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authenticationService, times(1)).signUp(any(RegisterDto.class));
    }

    @Test
    void authenticate_ShouldReturnLoginResponse_WhenValidCredentials() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        String fakeToken = "fake-jwt-token";
        long fakeExpiration = 3600000L;

        when(authenticationService.authenticate(any(LoginDto.class))).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn(fakeToken);
        when(jwtService.getExpirationTime()).thenReturn(fakeExpiration);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken))
                .andExpect(jsonPath("$.expiresIn").value(fakeExpiration)); // Проверь, как точно называются поля в твоем LoginResponse (например token или jwtToken)
    }

    @Test
    void verifyUser_ShouldReturnSuccessMessage_WhenVerificationSucceeds() throws Exception {
        VerifyDto verifyDto = new VerifyDto();
        verifyDto.setEmail("test@example.com");
        verifyDto.setVerificationCode("123456");

        doNothing().when(authenticationService).verifyUser(any(VerifyDto.class));

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account verified successfully"));
    }

    @Test
    void verifyUser_ShouldReturnBadRequest_WhenRuntimeExceptionThrown() throws Exception {
        VerifyDto verifyDto = new VerifyDto();
        verifyDto.setEmail("test@example.com");
        verifyDto.setVerificationCode("wrong-code");

        doThrow(new RuntimeException("Invalid verification code"))
                .when(authenticationService).verifyUser(any(VerifyDto.class));

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid verification code"));
    }

    @Test
    void resendVerification_ShouldReturnSuccessMessage_WhenEmailExists() throws Exception {
        String email = "test@example.com";

        doNothing().when(authenticationService).resendVerificationCode(email);

        mockMvc.perform(post("/auth/resend")
                        .param("email", email)) // Передаем как @RequestParam
                .andExpect(status().isOk())
                .andExpect(content().string("Verification Code sent"));
    }

    @Test
    void resendVerification_ShouldReturnBadRequest_WhenServiceFails() throws Exception {
        String email = "notfound@example.com";

        doThrow(new RuntimeException("User not found"))
                .when(authenticationService).resendVerificationCode(email);

        mockMvc.perform(post("/auth/resend")
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }
}