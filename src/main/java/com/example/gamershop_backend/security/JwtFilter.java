package com.example.gamershop_backend.security;

import com.example.gamershop_backend.security.JwtUtils;
import com.example.gamershop_backend.service.MyUserDetailService.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final MyUserDetailService userDetailService;

    public JwtFilter(JwtUtils jwtUtils, MyUserDetailService userDetailService) {
        this.jwtUtils = jwtUtils;
        this.userDetailService = userDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtils.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Token invalido");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailService.loadUserByUsername(username);

            if (jwtUtils.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Aquí es donde Spring "aprende" quién es el usuario
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}