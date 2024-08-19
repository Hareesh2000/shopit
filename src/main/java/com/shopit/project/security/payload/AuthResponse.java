package com.shopit.project.security.payload;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {
    private Long userId;
    private String username;
    private List<String> roles;
    private String jwt;
    private String refreshTokenCookie;

    public AuthResponse(Long userId, String username, List<String> roles,
                        String jwt, String refreshToken) {
        this.username = username;
        this.roles = roles;
        this.userId = userId;
        this.jwt = jwt;
        this.refreshTokenCookie = refreshToken;
    }

    public AuthResponse() {

    }
}