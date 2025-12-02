package com.example.gamershop_backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Validar si existe el encabezado Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // 2. Validar el Token
            if (jwtUtils.validateToken(jwt)) {
                username = jwtUtils.extractUsername(jwt);

                // --- INICIO DEL ARREGLO ---

                // Extraer el rol del token (clave "role")
                String role = jwtUtils.extractClaim(jwt, claims -> (String) claims.get("role"));

                // Si no tiene rol, asignamos USER por defecto
                if (role == null) {
                    role = "USER";
                }

                // Crear la autoridad con el prefijo "ROLE_" necesario para Spring Security
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                // Crear el usuario CON la autoridad (ya no es lista vacía)
                UserDetails userDetails = new User(username, "", List.of(authority));

                // --- FIN DEL ARREGLO ---

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Ignoramos errores de token para no romper la petición con 500
        }

        filterChain.doFilter(request, response);
    }
}