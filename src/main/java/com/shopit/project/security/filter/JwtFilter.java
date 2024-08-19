package com.shopit.project.security.filter;

import com.shopit.project.security.service.JwtService;
import com.shopit.project.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter { //executed only once for a given request

    private final JwtService jwtService;

    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public JwtFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());

        String path = request.getRequestURI();

        // Skip the filter for authentication and public endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/api/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = parseJwt(request);

        String username = jwtService.getUserNameFromJwt(jwt);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails,
                        null,
                        userDetails.getAuthorities());
        logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

        authentication.setDetails
                (new WebAuthenticationDetailsSource()
                        .buildDetails(request)); //sets request-specific information

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response); //Causes the next filter in the chain to be invoked
    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtService.getJwtFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        jwtService.validateJwt(jwt);
        return jwt;
    }
}

