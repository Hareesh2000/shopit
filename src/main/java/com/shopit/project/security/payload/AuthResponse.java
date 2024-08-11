package com.shopit.project.security.payload;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {
    private Long userId;
    private String username;
    private List<String> roles;
    private String jwtCookie;

    public AuthResponse(Long userId, String username, List<String> roles, String jwtCookie) {
        this.username = username;
        this.roles = roles;
        this.userId = userId;
        this.jwtCookie = jwtCookie;
    }

    public AuthResponse() {

    }
}