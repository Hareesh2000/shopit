package com.shopit.project.security.service;

import com.shopit.project.exceptions.APIException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private final String jwtSecret;

    public JwtServiceImpl() {
        jwtSecret = generateSecretKey();
    }

    private String generateSecretKey(){
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    private Key key() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    public String generateJwtFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwt(String jwt) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(jwt)
                .getPayload().getSubject();
    }

    public void validateJwt(String jwt) {
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(jwt);
        } catch (MalformedJwtException e) {
            throw new APIException("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            throw new APIException("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new APIException("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new APIException("JWT claims string is empty: " + e.getMessage());
        } catch (Exception e) {
            throw new APIException("Validation Failed: " + e.getMessage());
        }
    }

}