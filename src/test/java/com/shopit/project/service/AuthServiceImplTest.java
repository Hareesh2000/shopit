package com.shopit.project.service;

import com.shopit.project.exceptions.AuthenticationException;
import com.shopit.project.model.Role;
import com.shopit.project.model.RoleType;
import com.shopit.project.model.User;
import com.shopit.project.repository.CartRepository;
import com.shopit.project.repository.RoleRepository;
import com.shopit.project.repository.UserRepository;
import com.shopit.project.security.model.UserDetailsImpl;
import com.shopit.project.security.payload.AuthResponse;
import com.shopit.project.security.payload.LoginRequest;
import com.shopit.project.security.payload.SignupRequest;
import com.shopit.project.security.payload.SignupResponse;
import com.shopit.project.security.service.JwtService;
import com.shopit.project.security.service.RefreshTokenService;
import com.shopit.project.util.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private SignupResponse signupResponse;

    @Mock
    private AuthUtil authUtil;

    @Test
    void testRegisterUser_Success() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testUser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password");

        when(userRepository.existsByUserName("testUser")).thenReturn(false);
        when(userRepository.existsByUserEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.ROLE_USER))
                .thenReturn(Optional.of(new Role(RoleType.ROLE_USER)));
        when(roleRepository.findByRoleName(RoleType.ROLE_SELLER))
                .thenReturn(Optional.of(new Role(RoleType.ROLE_SELLER)));
        when(roleRepository.findByRoleName(RoleType.ROLE_ADMIN))
                .thenReturn(Optional.of(new Role(RoleType.ROLE_ADMIN)));
        authService.registerUser(signupRequest);

        verify(signupResponse).setSuccess("true");
        verify(signupResponse).setMessage("User registered successfully!");
    }

    @Test
    void testRegisterUser_UsernameAlreadyTaken() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testUser");

        when(userRepository.existsByUserName("testUser")).thenReturn(true);

        authService.registerUser(signupRequest);

        verify(signupResponse).setSuccess("false");
        verify(signupResponse).setMessage("Error: Username is already taken!");
    }

    @Test
    void testAuthenticateUser_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password");

        UserDetailsImpl userDetails = new UserDetailsImpl();
        List<GrantedAuthority> grantedAuthorities =
                new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        userDetails.setUserId(1L);
        userDetails.setUsername("testUser");
        userDetails.setAuthorities(grantedAuthorities);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateJwtFromUsername("testUser")).thenReturn("jwtToken");
        when(refreshTokenService.generateRefreshTokenCookie("testUser")).thenReturn("refreshTokenCookie");

        AuthResponse response = authService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
        assertEquals("jwtToken", response.getJwt());
        assertEquals("refreshTokenCookie", response.getRefreshTokenCookie());
    }

    @Test
    void testAuthenticateUser_BadCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException(""));

        assertThrows(AuthenticationException.class,
                () -> authService.authenticateUser(loginRequest),"Error: Bad credentials!");
    }

    @Test
    void testGenerateLogoutCookie() {
        ResponseCookie responseCookie = mock(ResponseCookie.class);
        when(refreshTokenService.generateCleanCookie()).thenReturn(responseCookie);

        ResponseCookie result = authService.generateLogoutCookie();

        assertNotNull(result);
        assertEquals(responseCookie, result);
    }

    @Test
    void testUnAuthenticateUser() {
        User user = mock(User.class);
        when(authUtil.loggedInUser()).thenReturn(user);

        authService.unAuthenticateUser();

        verify(refreshTokenService).invalidateRefreshToken(user);
    }
}