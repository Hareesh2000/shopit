package com.shopit.project.security.filter;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.security.service.JwtService;
import com.shopit.project.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @Test
    void testDoFilterInternal_WithValidJwt() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/admin/endpoint");
        when(jwtService.getJwtFromHeader(request)).thenReturn("valid-jwt");
        when(jwtService.getUserNameFromJwt("valid-jwt")).thenReturn("testUser");
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).validateJwt("valid-jwt");
        verify(jwtService).getUserNameFromJwt("valid-jwt");
        verify(userDetailsService).loadUserByUsername("testUser");
        verify(filterChain).doFilter(request, response);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithPublicEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/public/endpoint");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).getJwtFromHeader(any(HttpServletRequest.class));
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithAuthEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/auth/endpoint");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).getJwtFromHeader(any(HttpServletRequest.class));
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithInvalidJwt() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(jwtService.getJwtFromHeader(request)).thenReturn("invalid-jwt");
        doThrow(new APIException("Invalid JWT token")).when(jwtService).validateJwt("invalid-jwt");

        assertThrows(APIException.class, () -> jwtFilter.doFilterInternal(request, response, filterChain));

        verify(jwtService).validateJwt("invalid-jwt");
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}