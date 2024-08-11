package com.shopit.project.service;

import com.shopit.project.security.payload.UserInfoResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
    String getUsername(Authentication authentication);

    UserInfoResponse getUser(Authentication authentication);
}
