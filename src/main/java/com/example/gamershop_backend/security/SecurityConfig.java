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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // =============================================================
                        // 1. REGLA DE ORO: PERMITIR PREFLIGHT (OPTIONS)
                        // =============================================================
                        // Esto evita errores CORS falsos en la nube
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // =============================================================
                        // 2. ZONA PÚBLICA (Invitados)
                        // =============================================================
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // PRODUCTOS Y CATEGORÍAS: Permitir GET explícitamente
                        // Agregamos sin y con slash final para evitar errores tontos
                        .requestMatchers(HttpMethod.GET, "/productos", "/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/**").permitAll()

                        // =============================================================
                        // 3. ZONA DE USUARIOS
                        // =============================================================
                        .requestMatchers(HttpMethod.GET, "/usuarios/perfil").authenticated()
                        .requestMatchers(HttpMethod.POST, "/ordenes").authenticated()

                        // =============================================================
                        // 4. ZONA DE ADMIN (Bloqueo fuerte)
                        // =============================================================
                        // Gestión de usuarios y órdenes completa
                        .requestMatchers("/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/ordenes/**").hasAuthority("ADMIN")

                        // Modificar catálogo (POST, PUT, DELETE en general)
                        .requestMatchers(HttpMethod.POST, "/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")

                        // =============================================================
                        // 5. EL RESTO
                        // =============================================================
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // En producción idealmente pon tu dominio de front
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        config.setExposedHeaders(List.of("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1 hora de caché para opciones

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}