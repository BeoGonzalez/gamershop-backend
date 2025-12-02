package com.example.gamershop_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Clase de utilidades para la generación, extracción y validación de JSON Web Tokens (JWT).
 */
@Component
public class JwtUtils {

    // CLAVE SECRETA: Debe ser larga y compleja. En producción, debe cargarse desde variables de entorno.
    private static final String SECRET_KEY = "esta_es_una_clave_secreta_muy_larga_para_que_funcione_hs256";
    private static final long EXPIRATION_TIME = 86400000; // 24 horas en milisegundos

    private Key getSigningKey() {
        // Genera la clave de firma usando la clave secreta en bytes
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Genera un JWT para un usuario y su rol.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // Añade el rol como un claim personalizado
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el nombre de usuario (subject) del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token usando una función resolver.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Valida si el token es válido (no expirado y firma correcta).
     */
    public boolean validateToken(String token) {
        try {
            // Intenta parsear y validar la firma. Si falla, lanza una excepción.
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Manejo de errores de JWT (firma inválida, token expirado, etc.)
            return false;
        }
    }
}