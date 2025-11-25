package com.rmh.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenExpiryMillis;
    private final long accessTokenExpirySeconds;

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.accessTokenExpirationSeconds}") long accessSeconds
    ){
        if (secret == null || secret.length() < 64) {
            throw new IllegalStateException("JWT secret must be configured and at least 64 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirySeconds = accessSeconds;
        this.accessTokenExpiryMillis = accessSeconds * 1000;
    }

    public String generateAccessToken(String username, Set<String> roles){
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .setSubject(username)
            .claim("roles", String.join(",", roles))
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + accessTokenExpiryMillis))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex){
            return false;
        }
    }

    public String getSubjectFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }
}
