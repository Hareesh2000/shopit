package com.shopit.project.service;

import com.shopit.project.exceptions.AuthenticationException;
import com.shopit.project.security.model.UserDetailsImpl;
import com.shopit.project.security.payload.UserInfoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private Authentication authentication;

    @Test
    void testGetUsername_WithValidAuthentication() {
        when(authentication.getName()).thenReturn("testUser");

        String result = userService.getUsername(authentication);

        assertEquals("testUser", result);
    }

    @Test
    void testGetUsername_WithNullAuthentication() {
        assertThrows(AuthenticationException.class, () -> userService.getUsername(null));
    }

    @Test
    void testGetUser_WithValidAuthentication() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        List<GrantedAuthority> grantedAuthorities =
                new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        userDetails.setUserId(1L);
        userDetails.setUsername("testUser");
        userDetails.setAuthorities(grantedAuthorities);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        UserInfoResponse result = userService.getUser(authentication);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testUser", result.getUsername());
        assertTrue(result.getRoles().contains("ROLE_USER"));
    }

    @Test
    void testGetUser_WithNullAuthentication() {
        assertThrows(AuthenticationException.class, () -> userService.getUser(null));
    }
}