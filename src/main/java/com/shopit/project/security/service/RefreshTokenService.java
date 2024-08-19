package com.shopit.project.security.service;

import com.shopit.project.model.User;
import com.shopit.project.security.payload.RefreshJwtResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public interface RefreshTokenService {
    String hashRefreshToken(String refreshToken);

    String generateRefreshTokenCookie(String username);

    RefreshJwtResponse refreshJwt(HttpServletRequest request);

    ResponseCookie generateCleanCookie();

    void invalidateRefreshToken(User user);
}
