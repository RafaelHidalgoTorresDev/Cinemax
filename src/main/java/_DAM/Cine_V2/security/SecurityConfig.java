package _DAM.Cine_V2.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity // 🔥 Permite usar @PreAuthorize en los controladores
@RequiredArgsConstructor
public class SecurityConfig {

    // Inyectamos el filtro JWT
    private final JwtAuthenticationFilter jwtFilter;
    // Inyectamos los handlers de error personalizados
    private final CustomAuthenticationEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🌐 Habilitamos CORS para el frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ❌ Desactivamos CSRF (API REST stateless)
                .csrf(csrf -> csrf.disable())

                // 1. 🛡️ SESIÓN STATELESS — Cada petición trae su token, no guardamos sesión
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. 🔐 CONFIGURACIÓN DE RUTAS PÚBLICAS Y PROTEGIDAS
                .authorizeHttpRequests(auth -> auth
                        // 🔓 URLs públicas según el enunciado
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // 🔓 Cartelera pública: GET películas y funciones (sesiones)
                        .requestMatchers(HttpMethod.GET, "/api/v1/peliculas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/funciones/**").permitAll()

                        // 🔒 Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // 3. 🚨 MANEJO DE ERRORES — Devuelve JSON en vez de HTML
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)    // Error 401
                        .accessDeniedHandler(accessDeniedHandler)     // Error 403
                )

                // 4. 👮 FILTRO JWT — Se ejecuta antes del filtro de autenticación clásico
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración de CORS para permitir peticiones desde el frontend React (Vite).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}