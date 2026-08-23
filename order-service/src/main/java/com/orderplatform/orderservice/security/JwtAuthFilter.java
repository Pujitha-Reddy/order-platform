package com.orderplatform.orderservice.security;

import com.orderplatform.orderservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                UUID userId = jwtService.validateAndGetUserId(header.substring(7));
                request.setAttribute("userId", userId);
            } catch (Exception e) {
                // Invalid or expired token — leave userId unset. Controllers
                // that require auth check for its absence themselves.
            }
        }

        chain.doFilter(request, response);
    }
}