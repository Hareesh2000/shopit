package com.shopit.project.security.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.security.model.RefreshToken;
import com.shopit.project.model.User;
import com.shopit.project.repository.RefreshTokenRepository;
import com.shopit.project.repository.UserRepository;
import com.shopit.project.security.payload.RefreshJwtResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;


@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${spring.app.refreshTokenExpirationMinutes}")
    private int refreshTokenExpirationMinutes;

    @Value("${spring.app.refreshTokenCookieName}")
    private String refreshTokenCookieName;

    private final String refreshTokenSecret;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final JwtService jwtService;

    public RefreshTokenServiceImpl(UserRepository userRepository,
                                   RefreshTokenRepository refreshTokenRepository,
                                   JwtService jwtService){
        this.refreshTokenSecret = generateSecretKey();
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    private static String generateSecretKey(){
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    private RefreshToken validateRefreshToken(String refreshToken) {

        String hashedRefreshToken = hashRefreshToken(refreshToken);

        RefreshToken savedRefreshToken = refreshTokenRepository.findByRefreshToken(hashedRefreshToken)
                .orElseThrow(() -> new APIException("Invalid refresh token"));

        if(savedRefreshToken.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new APIException("Refresh Token Expired");

        return savedRefreshToken;
    }

    private String generateAndStoreRefreshToken(String username) {
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        String refreshTokenString = base64Encoder.encodeToString(keyBytes);
        String hashedRefreshToken;
        try {
            hashedRefreshToken = hashRefreshToken(refreshTokenString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDateTime createdDate = LocalDateTime.now();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(refreshTokenExpirationMinutes);

        RefreshToken refreshToken = user.getRefreshToken();
        if(refreshToken == null){
            refreshToken = new RefreshToken();
        }

        refreshToken.setRefreshToken(hashedRefreshToken);
        refreshToken.setCreatedDate(createdDate);
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setUser(user);
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        user.setRefreshToken(savedRefreshToken);
        userRepository.save(user);

        return refreshTokenString;
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie refreshTokenCookie = WebUtils.getCookie(request, refreshTokenCookieName);
        if (refreshTokenCookie == null) {
            throw new APIException("Refresh token Cookie not found");
        }

        return refreshTokenCookie.getValue();
    }

    @Override
    public String hashRefreshToken(String refreshToken) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(refreshTokenSecret.getBytes(), "HmacSHA256");

        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            mac.init(secretKeySpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] hmac = mac.doFinal(refreshToken.getBytes());

        return Base64.getEncoder().encodeToString(hmac);
    }

    @Override
    public String generateRefreshTokenCookie(String username) {
        String refreshToken = generateAndStoreRefreshToken(username);
        ResponseCookie refreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, refreshToken)
                .path("/api/auth/refresh-jwt")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
        return refreshTokenCookie.toString();
    }

    @Override
    public RefreshJwtResponse refreshJwt(HttpServletRequest request) {
        String refreshTokenString = getRefreshTokenFromCookie(request);

        RefreshToken refreshToken = validateRefreshToken(refreshTokenString);

        String username = refreshToken.getUser().getUserName();
        String jwt = jwtService.generateJwtFromUsername(username);

        RefreshJwtResponse refreshJwtResponse = new RefreshJwtResponse();
        refreshJwtResponse.setJwt(jwt);

        return refreshJwtResponse;
    }

    @Override
    public ResponseCookie generateCleanCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .path("/api/auth/refresh-jwt")
                .build();
    }

    @Transactional
    @Override
    public void invalidateRefreshToken(User user) {
        user.setRefreshToken(null);
        userRepository.save(user);
    }

}
