package com.example.gamershop_backend.security;

import com.example.gamershop_backend.service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
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
    private final MyUserDetailService myUserDetailService;

    // Usamos @Lazy para evitar referencias circulares si las hubiera
    public JwtFilter(JwtUtils jwtUtils, @Lazy MyUserDetailService myUserDetailService) {
        this.jwtUtils = jwtUtils;
        this.myUserDetailService = myUserDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 1. Extraer el token del Header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtils.extractUsername(token);
            } catch (Exception e) {
                System.out.println("❌ [JwtFilter] Error extrayendo usuario del token: " + e.getMessage());
            }
        }

        // 2. Si hay usuario pero no hay autenticación en el contexto actual
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. ¡VITAL! Cargamos los detalles COMPLETOS (incluyendo ROLES) desde la BD
            UserDetails userDetails = myUserDetailService.loadUserByUsername(username);

            // 4. Validamos el token
            if (jwtUtils.validateToken(token, userDetails)) {

                // DIAGNÓSTICO EN CONSOLA (Para que veas que está pasando)
                System.out.println("✅ [JwtFilter] Usuario autenticado: " + username);
                System.out.println("✅ [JwtFilter] Roles cargados: " + userDetails.getAuthorities());

                // 5. Creamos la autenticación CON LOS ROLES (userDetails.getAuthorities())
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // <--- ¡AQUÍ ESTABA EL PROBLEMA!
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Establecemos la seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}