package com.rubaet.agri.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    /** Access token: 1 hour. Read from secrets.properties via application.properties. */
    private static final long ACCESS_TOKEN_EXPIRY_MS = 1000L * 60 * 60; // 1 hour

    /** Refresh token: 7 days. */
    private static final long REFRESH_TOKEN_EXPIRY_MS = 1000L * 60 * 60 * 24 * 7;

    @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    // ── Public helpers ────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    /** Generates a short-lived ACCESS token (1 hour). */
    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, ACCESS_TOKEN_EXPIRY_MS);
    }

    /** Generates a long-lived REFRESH token (7 days). */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "refresh");
        return buildToken(extraClaims, userDetails, REFRESH_TOKEN_EXPIRY_MS);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        return "refresh".equals(claims.get("type", String.class));
    }

    // ── Private helpers ───────────────────────────────────────────

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiryMs) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
