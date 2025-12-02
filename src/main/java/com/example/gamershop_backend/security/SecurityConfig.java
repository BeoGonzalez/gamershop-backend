package com.example.gamershop_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtEntryPoint; // NUEVO: Inyectamos el manejador de errores

    // Constructor actualizado para recibir el EntryPoint
    public SecurityConfig(JwtFilter jwtFilter, JwtAuthenticationEntryPoint jwtEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.jwtEntryPoint = jwtEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. MANEJO DE EXCEPCIONES: Aquí conectamos tu clase "Chivato"
                // Si falla la auth o los permisos, Spring llamará a jwtEntryPoint.commence()
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        // 2. RUTAS PÚBLICAS
                        // OPTIONS: Necesario para que el navegador pregunte permisos (CORS) sin autenticarse
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Auth: Login y Registro libres para todos
                        .requestMatchers("/auth/**").permitAll()
                        // Productos: GET libre para ver el catálogo
                        .requestMatchers(HttpMethod.GET, "/api/producto/**").permitAll()

                        // 3. RUTAS DE ADMIN (Modificaciones de datos)
                        // Requieren que el usuario tenga ROLE_ADMIN en su token
                        .requestMatchers(HttpMethod.POST, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/producto/**").hasRole("ADMIN")

                        // 4. RESTO DE RUTAS
                        .anyRequest().authenticated()
                )
                // Configuración Stateless (JWT no usa cookies de sesión)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Añadir el filtro JWT antes del filtro estándar
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Orígenes permitidos (Localhost para pruebas y Vercel para producción)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://gamer-shop-sqvu.vercel.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}