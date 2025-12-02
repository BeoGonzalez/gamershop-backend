package com.example.gamershop_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http)) // Habilitar CORS
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas Públicas (Login y Registro)
                        // Estas rutas NO dan 403 ni 404 si el controller AuthController tiene @RequestMapping("/auth")
                        .requestMatchers("/auth/**").permitAll()

                        // Permitir solicitudes OPTIONS (necesario para que React no reciba error de CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Rutas SOLO para ADMIN (Operaciones destructivas)
                        // IMPORTANTE: Aquí actualizamos la ruta de "/carrito" a "/api/producto"
                        .requestMatchers(HttpMethod.DELETE, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/producto/**").hasRole("ADMIN")

                        // 3. Rutas para AMBOS (Ver productos)
                        // Cualquier usuario logueado (USER o ADMIN) puede ver la lista
                        .requestMatchers(HttpMethod.GET, "/api/producto/**").authenticated()

                        // 4. Todo lo demás requiere login
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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