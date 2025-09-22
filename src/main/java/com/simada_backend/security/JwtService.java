package com.simada_backend.security;

import com.simada_backend.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final Key key;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration}") long expirationMs,
            @Value("${app.security.jwt.issuer}") String issuer
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generate(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(user.getEmail())
                .addClaims(Map.of(
                        "uid", user.getId(),
                        "utype", String.valueOf(user.getUserType()),
                        "name", String.valueOf(user.getName()),
                        "photo", String.valueOf(user.getPhoto())
                ))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parser(token).getBody().getSubject();
    }

    public boolean isValid(String token) {
        try { parser(token); return true; } catch (JwtException | IllegalArgumentException e) { return false; }
    }

    private Jws<Claims> parser(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
