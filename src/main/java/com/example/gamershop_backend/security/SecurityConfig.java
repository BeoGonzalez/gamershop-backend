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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // =============================================================
                        // 2. ZONA PÚBLICA (Invitados)
                        // =============================================================
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/usuarios/login", "/usuarios/registro").permitAll()

                        // Catálogo público (Solo lectura)
                        // Aquí dejamos /** porque GET suele ser más permisivo,
                        // pero para blindarlo agregamos también la raíz explícita
                        .requestMatchers(HttpMethod.GET, "/productos", "/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/**").permitAll()

                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // =============================================================
                        // 3. ZONA DE USUARIO LOGUEADO
                        // =============================================================
                        .requestMatchers("/usuarios/perfil").authenticated()
                        .requestMatchers(HttpMethod.POST, "/ordenes").authenticated()
                        .requestMatchers(HttpMethod.GET, "/ordenes/mis-compras/**").authenticated()

                        // =============================================================
                        // 4. ZONA DE ADMIN (CRUD y Gestión) -> ¡AQUÍ ESTABA EL ERROR!
                        // =============================================================
                        // Agregamos "/productos" y "/categorias" SIN asteriscos para atrapar el POST

                        // --- PRODUCTOS ---
                        .requestMatchers(HttpMethod.POST, "/productos", "/productos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/productos", "/productos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/productos", "/productos/**").hasAuthority("ADMIN")

                        // --- CATEGORÍAS ---
                        .requestMatchers(HttpMethod.POST, "/categorias", "/categorias/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categorias", "/categorias/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categorias", "/categorias/**").hasAuthority("ADMIN")

                        // --- USUARIOS Y ORDENES ---
                        .requestMatchers("/usuarios", "/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/ordenes", "/ordenes/**").hasAuthority("ADMIN")

                        // =============================================================
                        // 5. CUALQUIER OTRA COSA
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
        config.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://*.onrender.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        config.setExposedHeaders(List.of("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}