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

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (No necesario para APIs Stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Configurar CORS explícitamente usando nuestro Bean de abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Gestión de sesión sin estado (Stateless)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Reglas de Autorización
                .authorizeHttpRequests(auth -> auth
                        // A. PERMITIR SIEMPRE (Login, Registro y Pre-flight de CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ¡CRUCIAL PARA EL 403 DE CORS!
                        .requestMatchers("/auth/**").permitAll()

                        // B. RUTAS DE PRODUCTOS
                        // GET: Visible para todos (o cámbialo a .authenticated() si prefieres)
                        .requestMatchers(HttpMethod.GET, "/api/producto/**").permitAll()

                        // C. RUTAS DE ADMIN (Requieren Rol)
                        // hasRole("ADMIN") verifica que el usuario tenga la autoridad "ROLE_ADMIN"
                        .requestMatchers(HttpMethod.POST, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/producto/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/producto/**").hasRole("ADMIN")

                        // D. Resto requiere autenticación
                        .anyRequest().authenticated()
                )

                // 5. Añadir el filtro JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- CONFIGURACIÓN CORS ROBUSTA ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (Localhost y Vercel)
        // Asegúrate de que no haya barras al final de las URL
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://gamer-shop-sqvu.vercel.app"
        ));

        // Métodos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Cabeceras permitidas
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));

        // Permitir credenciales
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