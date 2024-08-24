package com.shopit.project.security.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.model.User;
import com.shopit.project.repository.RefreshTokenRepository;
import com.shopit.project.repository.UserRepository;
import com.shopit.project.security.model.RefreshToken;
import com.shopit.project.security.payload.RefreshJwtResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Cookie cookie;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMinutes", 60);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenCookieName", "refreshToken");
    }

    @Test
    void testGenerateRefreshTokenCookie() {
        User user = new User();
        user.setUserName("testUser");

        when(userRepository.findByUserName("testUser")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        String refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie("testUser");

        assertNotNull(refreshTokenCookie);
        assertTrue(refreshTokenCookie.contains("refreshToken"));
    }

    @Test
    @Order(2)
    void testRefreshJwt_ValidRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(new User("testUser","testUser@mail.com","testPass"));
        refreshToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(cookie.getName()).thenReturn("refreshToken");
        when(cookie.getValue()).thenReturn("validToken");

        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateJwtFromUsername("testUser")).thenReturn("newJwt");

        RefreshJwtResponse response = refreshTokenService.refreshJwt(request);

        assertNotNull(response);
        assertEquals("newJwt", response.getJwt());
    }

    @Test
    void testInvalidateRefreshToken() {
        User user = new User();
        user.setUserName("testUser");

        refreshTokenService.invalidateRefreshToken(user);

        verify(userRepository, times(1)).save(user);
        assertNull(user.getRefreshToken());
    }

    @Test
    void testGetRefreshTokenFromCookie_ThrowsExceptionWhenCookieNotFound() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        APIException exception = assertThrows(APIException.class, () -> refreshTokenService.refreshJwt(request));

        assertEquals("Refresh token Cookie not found", exception.getMessage());
    }

    @Test
    void testValidateRefreshToken_ThrowsExceptionWhenTokenNotFound() {
        Cookie cookie = new Cookie("refreshToken", "validRefreshTokenValue");

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> refreshTokenService.refreshJwt(request));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void testValidateRefreshToken_ThrowsExceptionWhenTokenExpired() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken("validToken");
        refreshToken.setExpiryDate(LocalDateTime.now().minusMinutes(10));

        Cookie cookie = new Cookie("refreshToken", "validRefreshTokenValue");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.of(refreshToken));

        APIException exception = assertThrows(APIException.class, () -> refreshTokenService.refreshJwt(request));

        assertEquals("Refresh Token Expired", exception.getMessage());
    }

    @Test
    @Order(1)
    void testHashRefreshToken() {
        String refreshToken = "someToken";
        String hashedToken = refreshTokenService.hashRefreshToken(refreshToken);

        assertNotNull(hashedToken);
        assertNotEquals(refreshToken, hashedToken);
    }

    @Test
    void testGenerateCleanCookie() {
        ResponseCookie cleanCookie = refreshTokenService.generateCleanCookie();

        assertNotNull(cleanCookie);
        assertEquals("refreshToken", cleanCookie.getName());
        assertEquals("", cleanCookie.getValue());
    }
}