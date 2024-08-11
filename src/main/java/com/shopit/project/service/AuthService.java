package com.shopit.project.service;

import com.shopit.project.security.payload.*;
import org.springframework.http.ResponseCookie;

public interface AuthService {
    SignupResponse registerUser(SignupRequest registerUser);

    AuthResponse authenticateUser(LoginRequest loginRequest);

    ResponseCookie generateLogoutCookie();
}
