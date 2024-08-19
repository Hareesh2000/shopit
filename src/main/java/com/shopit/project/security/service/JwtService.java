package com.shopit.project.security.service;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtService {
    String getJwtFromHeader(HttpServletRequest request);

    String generateJwtFromUsername(String username);

    String getUserNameFromJwt(String jwt);

    void validateJwt(String jwt);


}
