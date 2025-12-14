package com.example.gamershop_backend.security;

import com.example.gamershop_backend.service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Importante para logs profesionales
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

    // Logger profesional (mejor que System.out.println)
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public JwtFilter(JwtUtils jwtUtils, MyUserDetailService userDetailService) {
        this.jwtUtils = jwtUtils;
        this.userDetailService = userDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            // 1. Obtener el header Authorization
            String authHeader = request.getHeader("Authorization");
            String username = null;
            String jwt = null;

            // 2. Validar formato "Bearer ..."
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7); // Quitar "Bearer "
                try {
                    username = jwtUtils.extractUsername(jwt);
                } catch (Exception e) {
                    // Esto pasa si el token expiró o está mal formado.
                    // NO lanzamos error aquí. Simplemente logueamos y dejamos que siga como anónimo.
                    logger.error("No se pudo extraer el usuario del token: {}", e.getMessage());
                }
            }

            // 3. Si hay usuario y NO está autenticado todavía en el contexto
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailService.loadUserByUsername(username);

                // 4. Validar que el token corresponda al usuario y no haya expirado
                if (jwtUtils.validateToken(jwt, userDetails)) {

                    // 5. Crear la autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. ESTABLECER LA AUTENTICACIÓN EN SPRING SECURITY
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            // Capturamos cualquier otro error inesperado para que no tumbe el servidor
            logger.error("Error crítico en autenticación de usuario: {}", e.getMessage());
        }

        // 7. SIEMPRE continuar con la cadena de filtros
        // Si el token era válido, Spring ya sabe quién eres.
        // Si no, sigues como "invitado" y SecurityConfig decidirá si te deja pasar o te da 403.
        chain.doFilter(request, response);
    }
}