package com.shopit.project.security.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {

    private Long userId;
    private String username;
    private List<String> roles;
    private String jwt;

    public LoginResponse(Long userId, String username, List<String> roles, String jwt) {
        this.username = username;
        this.roles = roles;
        this.userId = userId;
        this.jwt = jwt;
    }

}


