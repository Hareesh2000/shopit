package com.shopit.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.project.security.payload.*;
import com.shopit.project.security.service.RefreshTokenService;
import com.shopit.project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testGetCsrfToken() throws Exception {
        CsrfToken csrfToken = new CsrfToken() {
            @Override
            public String getHeaderName() {
                return "X-CSRF-TOKEN";
            }

            @Override
            public String getParameterName() {
                return "_csrf";
            }

            @Override
            public String getToken() {
                return "test-csrf-token";
            }
        };

        mockMvc.perform(get("/api/csrf-token").requestAttr("_csrf", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-csrf-token"));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setEmail("testuser@test.com");
        SignupResponse signupResponse = new SignupResponse();
        signupResponse.setSuccess("true");

        when(authService.registerUser(any(SignupRequest.class))).thenReturn(signupResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"));

        verify(authService, times(1)).registerUser(any(SignupRequest.class));
    }

    @Test
    void testRegisterUser_Failure() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setEmail("testuser@test.com");
        SignupResponse signupResponse = new SignupResponse();
        signupResponse.setSuccess("false");

        when(authService.registerUser(any(SignupRequest.class))).thenReturn(signupResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value("false"));

        verify(authService, times(1)).registerUser(any(SignupRequest.class));
    }

    @Test
    void testAuthenticateUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        AuthResponse authResponse = new AuthResponse(1L, "testuser",
                List.of("ROLE_USER"), "jwt-token",
                "refresh-token-cookie=someValue; Path=/; HttpOnly");

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        "refresh-token-cookie=someValue; Path=/; HttpOnly"));

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testUnAuthenticateUser() throws Exception {
        ResponseCookie logoutCookie = ResponseCookie.from("refreshToken", "").path("/").build();

        when(authService.generateLogoutCookie()).thenReturn(logoutCookie);

        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, logoutCookie.toString()))
                .andExpect(jsonPath("$.message").value("You've been signed out successfully!"));

        verify(authService, times(1)).unAuthenticateUser();
    }

    @Test
    void testRefreshJwt() throws Exception {
        RefreshJwtResponse refreshJwtResponse = new RefreshJwtResponse();
        refreshJwtResponse.setJwt("new-jwt-token");

        when(refreshTokenService.refreshJwt(any(HttpServletRequest.class))).thenReturn(refreshJwtResponse);

        mockMvc.perform(post("/api/auth/refresh-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("new-jwt-token"));

        verify(refreshTokenService, times(1)).refreshJwt(any(HttpServletRequest.class));
    }
}