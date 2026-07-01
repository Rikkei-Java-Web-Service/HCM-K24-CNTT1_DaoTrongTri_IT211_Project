package com.re.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.project.dto.request.LoginRequest;
import com.re.project.dto.request.RegisterRequest;
import com.re.project.dto.response.LoginResponse;
import com.re.project.dto.response.RegisterResponse;
import com.re.project.service.AuthService;
import com.re.project.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.re.project.security.TokenProvider tokenProvider;

    @MockitoBean
    private com.re.project.security.AuthenticationFilter authenticationFilter;

    @MockitoBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @MockitoBean
    private com.re.project.security.UnauthorizedEntryPoint unauthorizedEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("candidate1");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFullName("Nguyen Van A");
        validRegisterRequest.setEmail("nguyenvana@gmail.com");
        validRegisterRequest.setRole("CANDIDATE");
    }

    @Test
    void registerUser_Success() throws Exception {
        RegisterResponse response = RegisterResponse.builder()
                .message("Registration successful")
                .username("candidate1")
                .build();
        Mockito.when(authService.handleRegister(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("candidate1"))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void registerUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        validRegisterRequest.setEmail("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("candidate1", "password123");
        LoginResponse response = LoginResponse.builder()
                .message("Login successfully!")
                .accessToken("token123")
                .build();

        Mockito.when(authService.handleLogin(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.message").value("Login successfully!"));
    }

    // 4. Test Login Missing Body -> 400 Bad Request
    @Test
    void login_MissingBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // 5. Test Logout Success -> 200 OK
    @Test
    void logout_Success() throws Exception {
        // Mock request header for token
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));
    }
}
