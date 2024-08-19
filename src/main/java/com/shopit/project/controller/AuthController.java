package com.shopit.project.controller;

import com.shopit.project.security.payload.*;
import com.shopit.project.security.service.RefreshTokenService;
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

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
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

    @Transactional
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticateUser(loginRequest);

        LoginResponse loginResponse = new LoginResponse(authResponse.getUserId(),
                authResponse.getUsername(), authResponse.getRoles(), authResponse.getJwt());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.SET_COOKIE, authResponse.getRefreshTokenCookie());
        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }

    @Transactional
    @PostMapping(value = "/logout")
    public ResponseEntity<?> unAuthenticateUser() {
        authService.unAuthenticateUser();

        ResponseCookie refreshTokenCookie = authService.generateLogoutCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString())
                .body(new MessageResponse("You've been signed out successfully!"));
    }

    @PostMapping("/auth/refresh-jwt")
    public ResponseEntity<RefreshJwtResponse> refreshJwt(HttpServletRequest request) {
        RefreshJwtResponse refreshJwtResponse = refreshTokenService.refreshJwt(request);

        return new ResponseEntity<>(refreshJwtResponse, HttpStatus.OK);
    }
}
