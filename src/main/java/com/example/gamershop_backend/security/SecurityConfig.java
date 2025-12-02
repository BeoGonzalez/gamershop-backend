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
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtEntryPoint;

    public SecurityConfig(JwtFilter jwtFilter, JwtAuthenticationEntryPoint jwtEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.jwtEntryPoint = jwtEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactiva CSRF ya que es una API REST sin estado (STATELESS) y usa JWT.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Manejo de excepciones para errores 401/403
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS
                        // ===> CORRECCIÓN APLICADA AQUÍ: Hace la ruta raíz (/) pública <===
                        .requestMatchers("/").permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Pre-flight CORS
                        .requestMatchers("/auth/**").permitAll() // Login/Registro
                        .requestMatchers(HttpMethod.GET, "/api/producto/**").permitAll() // Ver productos

                        // 2. RUTAS DE ADMIN (Permisos por rol)
                        .requestMatchers(HttpMethod.POST, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/producto/**").hasRole("ADMIN")

                        // 3. RESTO (Requiere autenticación)
                        .anyRequest().authenticated()
                )
                // Configura la sesión como sin estado (STATELESS) para JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Agrega el filtro JWT antes del filtro de autenticación estándar de Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- CONFIGURACIÓN CORS (Permisiva) ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite CUALQUIER origen (*). Ideal para desarrollo.
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // --- Otros Beans Necesarios ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}