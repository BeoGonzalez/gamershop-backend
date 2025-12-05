package com.example.gamershop_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Habilitar CORS con nuestra configuración personalizada
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Deshabilitar CSRF (No necesario para APIs REST Stateless)
                .csrf(csrf -> csrf.disable())

                // 3. Configurar Permisos de Rutas
                .authorizeHttpRequests(auth -> auth
                        // --- ACCESO PÚBLICO ---
                        // Autenticación (Login/Registro)
                        .requestMatchers("/auth/**").permitAll()

                        // Ver Productos (Solo GET es público, POST/DELETE son privados)
                        .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()

                        // Documentación Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // --- ACCESO PRIVADO ---
                        // Cualquier otra ruta requiere Token JWT válido
                        .anyRequest().authenticated()
                )

                // 4. Gestión de Sesión Stateless (Sin cookies de sesión)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Agregar el Filtro JWT antes del filtro de autenticación de Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Dominios permitidos (Frontend Local y Producción)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://gamer-shop-sqvu.vercel.app" // Asegúrate de que esta sea tu URL exacta de Vercel
        ));

        // Métodos permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas (necesario para Authorization)
        configuration.setAllowedHeaders(List.of("*"));

        // Permitir credenciales
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}