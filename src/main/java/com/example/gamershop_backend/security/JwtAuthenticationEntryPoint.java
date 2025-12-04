package com.example.gamershop_backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Esto se imprime en la consola del servidor (IntelliJ / Render)
        System.out.println("❌ ERROR DE SEGURIDAD (401/403): " + authException.getMessage());
        System.out.println("   Ruta solicitada: " + request.getRequestURI());

        // Esto se envía al Frontend (React)
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{ \"error\": \"Acceso Denegado\", \"mensaje\": \"" + authException.getMessage() + "\" }");
    }
}