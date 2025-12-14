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
                        // 1. PREFLIGHT (Obligatorio para que React no falle)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. PÚBLICO: Auth y Catálogo (Solo lectura)
                        .requestMatchers("/auth/**", "/usuarios/login", "/usuarios/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/productos", "/productos/**").permitAll() // <--- GET es público
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/**").permitAll() // <--- GET es público
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 3. USUARIOS (Perfil y crear órdenes)
                        .requestMatchers("/usuarios/perfil").authenticated()
                        .requestMatchers("/ordenes/mis-compras/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/ordenes").authenticated()

                        // 4. ADMIN (EL CAMBIO ESTÁ AQUÍ)
                        // Al quitar HttpMethod.POST/PUT/DELETE, esta regla atrapa TODO lo que no sea GET.
                        // Y al poner las dos rutas ("/ruta" y "/ruta/**"), atrapamos la raíz y los hijos.
                        .requestMatchers("/productos", "/productos/**").hasAuthority("ADMIN")
                        .requestMatchers("/categorias", "/categorias/**").hasAuthority("ADMIN")
                        .requestMatchers("/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/ordenes/**").hasAuthority("ADMIN")

                        // 5. RESTO
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