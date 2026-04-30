package com.example.IRMS.config.jwt;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.IRMS.modules.admin_tools.enums.RoleType;
import com.example.IRMS.modules.admin_tools.services.StaffManagementService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component // Enables dependency injection
@Slf4j // Enables logging for the class
@RequiredArgsConstructor
public class JwtProvider {
    private final StaffManagementService rbacService;

    @Value ("${jwt.secret}")
    private String secret;

    @Value ("${jwt.access.expiration}")
    private long accessTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate access token
    public String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .claim("type", "ACCESS_TOKEN")
                .claim("userId", claims.get("userId"))
                .claim("email", claims.get("email"))
                .claim("roles", claims.getOrDefault("roles", List.of("ROLE_USER")))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
            .verifyWith(getSigningKey()) 
            .build() 
            .parseSignedClaims(token); // Verify the token
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT");
        } catch (SecurityException e) {
            log.warn("Invalid signature");
        } catch (IllegalArgumentException e) {
            log.warn("Empty claims string");
        }
        return false;
    }


    // Get Claims
    // Structure of token:
    // header: {alg: HS256, typ: JWT}
    // payload: {username, roles, exp}
    public Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey()) 
            .build() 
            .parseSignedClaims(token).getPayload(); // Get the claims from the token
    }

    public String getType(String token) {
        return getClaims(token).get("type", String.class);
    }

    // Get authentication from token - try cookie first, then Bearer header
    public String resolveToken(HttpServletRequest request) {
        // First, try to read from HttpOnly cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Fallback to Bearer header for Postman/mobile/service-to-service
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer "
        }
        return null;
    }
    
    public Authentication getAuthentication(String token) { 
        Claims claims = getClaims(token);  

        // Use userId claim instead of email
        String userId = claims.get("userId", String.class);
        if (userId == null) {
            throw new IllegalArgumentException("JWT token does not contain userId claim");
        }

        String role = claims.get("role", String.class);
        if (role == null) {
            throw new IllegalArgumentException("JWT token does not contain role claim");
        }

        String roleName = role.startsWith("ROLE_") ? role.substring(5) : role;
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(roleName);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT token contains unknown role: " + role, ex);
        }

        var authorities = rbacService.permissionsForRole(roleType)
                .stream()
                .map(permission -> new SimpleGrantedAuthority("PERM_" + permission.name()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType.name()));

        // Now userId will be returned by userDetails.getUsername()
        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    // Get userId from token
    public String getUserIdFromToken(String token) {
        return getClaims(token).get("userId", String.class);
    }
    // Get role from token
    public String getRoleFromToken(String token) {
        String role = getClaims(token).get("role", String.class);
        if (role == null) {
            throw new IllegalArgumentException("JWT token does not contain role claim");
        }
        return role; // Assuming a user has only one role, return the first one
    }


}