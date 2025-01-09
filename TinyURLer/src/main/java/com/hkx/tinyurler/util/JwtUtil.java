package com.hkx.tinyurler.util;

import com.hkx.tinyurler.exception.JwtValidationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private String secretKey = "tiny-urler-secret-key-must-be-at-least-32-bytes";

    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 4000 * 60 * 60))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 验证密钥
                .build()
                .parseClaimsJws(token) // 解析 JWT
                .getBody()
                .getExpiration() // 获取过期时间
                .before(new Date());
    }

    public boolean validateToken(String token, String email) {
        String extractedEmail = extractEmail(token);

        if (!email.equals(extractedEmail)) {
            throw new JwtValidationException("Invalid token: Email does not match.");
        }

        if (isTokenExpired(token)) {
            throw new JwtValidationException("Invalid token: Token has expired.");
        }

        return true;
    }

}
