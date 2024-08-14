package com.shopit.project.controller;

import com.shopit.project.security.payload.*;
import com.shopit.project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<CsrfToken> getCsrfToken(HttpServletRequest request) {
        return new ResponseEntity<>((CsrfToken) request.getAttribute("_csrf"), HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/auth/signup")
    public ResponseEntity<SignupResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse response = authService.registerUser(signupRequest);

        if(Objects.equals(response.getSuccess(), "false"))
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticateUser(loginRequest);

        UserInfoResponse userInfoResponse = new UserInfoResponse(authResponse.getUserId(),
                authResponse.getUsername(), authResponse.getRoles());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.SET_COOKIE, authResponse.getJwtCookie());
        return new ResponseEntity<>(userInfoResponse, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<?> unAuthenticateUser() {
        ResponseCookie cookie = authService.generateLogoutCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                cookie.toString())
                .body(new MessageResponse("You've been signed out successfully!"));
    }
}
