package com.splitwise.security;

import com.splitwise.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    System.out.println("JWT FILTER: " + request.getMethod() + " " + request.getRequestURI());

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        System.out.println("JWT FILTER: No token found");
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);

    try {

        if (jwtUtil.validateToken(token)) {

            String email = jwtUtil.extractEmail(token);

            System.out.println("JWT FILTER: Token valid for " + email);

            var userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {

                var user = userOptional.get();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }

    } catch (Exception e) {
        System.out.println("JWT FILTER ERROR: " + e.getMessage());
        SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
}
}