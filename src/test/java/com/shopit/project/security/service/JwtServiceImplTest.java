package com.shopit.project.security.service;

import com.shopit.project.exceptions.APIException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000); // Setting the expiration time field
    }

    @Test
    void testGetJwtFromHeader_ValidHeader() {
        when(request.getHeader("Authorization")).thenReturn("Bearer validJwtToken");

        String jwt = jwtService.getJwtFromHeader(request);

        assertEquals("validJwtToken", jwt);
    }

    @Test
    void testGetJwtFromHeader_NoBearer() {
        when(request.getHeader("Authorization")).thenReturn("invalidJwtToken");

        String jwt = jwtService.getJwtFromHeader(request);

        assertNull(jwt);
    }

    @Test
    @Order(1)
    void testGenerateJwtFromUsername() {
        String jwt = jwtService.generateJwtFromUsername("testUser");

        assertNotNull(jwt);
    }

    @Test
    @Order(2)
    void testGetUserNameFromJwt() {
        String jwt = jwtService.generateJwtFromUsername("testUser");

        String username = jwtService.getUserNameFromJwt(jwt);

        assertEquals("testUser", username);
    }

    @Test
    void testValidateJwt_ValidJwt() {
        String jwt = jwtService.generateJwtFromUsername("testUser");

        assertDoesNotThrow(() -> jwtService.validateJwt(jwt));
    }

    @Test
    void testValidateJwt_ExpiredJwt() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1); // Setting the expiration time in the past

        String jwt = jwtService.generateJwtFromUsername("testUser");

        APIException exception = assertThrows(APIException.class, () -> jwtService.validateJwt(jwt));

        assertTrue(exception.getMessage().contains("JWT token is expired"));
    }

    @Test
    void testValidateJwt_MalformedJwt() {
        APIException exception = assertThrows(APIException.class, () -> jwtService.validateJwt("malformedJwt"));

        assertTrue(exception.getMessage().contains("Invalid JWT token"));
    }

    @Test
    void testValidateJwt_IllegalArgumentJwt() {
        APIException exception = assertThrows(APIException.class, () -> jwtService.validateJwt(""));

        assertTrue(exception.getMessage().contains("JWT claims string is empty"));
    }
}