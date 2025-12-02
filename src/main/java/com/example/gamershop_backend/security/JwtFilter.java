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

/**
 * Filtro que intercepta las peticiones, extrae y valida el JWT
 * en el encabezado 'Authorization', y configura la autenticación
 * de Spring Security con el rol extraído del token.
 */
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

        // 1. Validar si existe el encabezado Authorization y si es un token Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // 2. Validar el Token
            if (jwtUtils.validateToken(jwt)) {
                username = jwtUtils.extractUsername(jwt);

                // Aseguramos que solo autenticamos si no hay ya una autenticación en el contexto
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 3. Extraer el rol del token (clave "role")
                    String role = jwtUtils.extractClaim(jwt, claims -> (String) claims.get("role"));

                    // Si el rol es nulo o vacío, asignamos "USER" por defecto
                    if (role == null || role.trim().isEmpty()) {
                        role = "USER";
                    }

                    // 4. Crear la autoridad CON el prefijo "ROLE_"
                    // Esto es VITAL para que hasRole("ADMIN") funcione correctamente.
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

                    // 5. Crear el objeto UserDetails con las autoridades
                    UserDetails userDetails = new User(username, "", List.of(authority));

                    // 6. Crear el token de autenticación de Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 7. Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Ignoramos errores de token (ej. expirado, inválido) para permitir que
            // la petición continúe y sea manejada por el JwtAuthenticationEntryPoint si es necesario.
            System.err.println("Error al procesar JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}