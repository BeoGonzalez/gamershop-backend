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
                        // 1. ZONA PÚBLICA (Invitados)
                        // =============================================================
                        // Login, Registro y Documentación API
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Ver productos y categorías es libre
                        .requestMatchers(HttpMethod.GET, "/productos/**", "/categorias/**").permitAll()

                        // =============================================================
                        // 2. ZONA DE USUARIOS REGISTRADOS (Cualquier Rol)
                        // =============================================================
                        // IMPORTANTE: Esta regla va ANTES del bloqueo general de /usuarios
                        .requestMatchers(HttpMethod.GET, "/usuarios/perfil").authenticated()

                        // Comprar (Crear orden)
                        .requestMatchers(HttpMethod.POST, "/ordenes").authenticated()

                        // =============================================================
                        // 3. ZONA DE ADMINISTRADOR (Gestión Total)
                        // =============================================================
                        // Escribir en Catálogo (Crear/Editar/Borrar Productos y Categorías)
                        // El /** bloquea cualquier POST/PUT/DELETE que no hayamos permitido arriba explícitamente
                        .requestMatchers(HttpMethod.POST, "/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")

                        // Gestión de Usuarios y Ver todas las Órdenes
                        .requestMatchers("/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/ordenes/**").hasAuthority("ADMIN")

                        // =============================================================
                        // 4. RESTO (Por seguridad, todo lo demás requiere login)
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
        // Permite conexión desde cualquier origen (Celular, Localhost, Producción)
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}