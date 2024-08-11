package com.shopit.project.controller;

import com.shopit.project.security.payload.UserInfoResponse;
import com.shopit.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/username")
    public ResponseEntity<?> getUserName(Authentication authentication) {
        String username = userService.getUsername(authentication);
        String response = "Username: " + username;
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public ResponseEntity<UserInfoResponse> getUser(Authentication authentication) {
        UserInfoResponse response = userService.getUser(authentication);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
