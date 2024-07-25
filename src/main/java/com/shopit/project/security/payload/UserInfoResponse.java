package com.shopit.project.security.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfoResponse {

    private Long userId;
    private String username;
    private List<String> roles;

    public UserInfoResponse(Long userId, String username, List<String> roles) {
        this.username = username;
        this.roles = roles;
        this.userId = userId;
    }

}


