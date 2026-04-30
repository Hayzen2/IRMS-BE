package com.example.IRMS.config.jwt;
import java.io.IOException;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@EnableWebSecurity
@RequiredArgsConstructor
// OncePerRequestFilter: Ensures the filter is executed once per request
// JwtAuthenticationFilter: Custom filter for JWT authentication
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override 
    protected void doFilterInternal(
        //Sets the user's authentication in Spring Security's context
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Get the token from the request header
        String token = jwtProvider.resolveToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate the token
            if (!jwtProvider.validateToken(token)) {
                throw new JwtException("Invalid JWT");
            }

            // Extracts the user's authentication details
            // Get Type of authentication
            Claims claims = jwtProvider.getClaims(token);
            String tokenType = claims.get("type", String.class);
            
            
            // Only ACCESS_TOKEN is valid for API calls
            if (!"ACCESS_TOKEN".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }
            Authentication auth = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException e) {
            // Preserve the specific auth failure reason for the authentication entry point.
            request.setAttribute("AUTH_ERROR", e.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response); // Pass control to the next filter
    }
}