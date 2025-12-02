package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // --- Configuración CORS ---
    // Esta configuración es correcta y permite todos los orígenes.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // --- Cadena de Filtros de Seguridad ---
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF (Deshabilitado, necesario para APIs REST sin tokens CSRF)
                .csrf(csrf -> csrf.disable())

                // 3. Sesión (Sin estado)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Autorización de Peticiones
                .authorizeHttpRequests(auth -> auth

                        // ⭐ REGLA CRÍTICA: PERMITIR TODOS LOS ENDPOINTS PÚBLICOS SIN RESTRICCIÓN
                        .requestMatchers(
                                // Rutas de Autenticación
                                "/api/auth/login",
                                "/api/auth/register",

                                // Rutas de Carrito (Si deben ser públicas)
                                "/api/cart/**",

                                // Rutas de Producto (Acceso GET es Público)
                                "/products",
                                "/products/**",
                                "/api/products",
                                "/api/products/**",

                                // Rutas de Swagger/Docs/Raíz
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/",
                                "/*.png", "/*.jpg", "/*.jpeg", "/*.html", "/*.css", "/*.js",
                                "/css/**",
                                "/js/**"
                        ).permitAll()

                        // ⭐ CRÍTICO: Permitir el método OPTIONS para peticiones CORS pre-flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Rutas que requieren GET pero que están envueltas en las anteriores
                        .requestMatchers(HttpMethod.GET, "/api/cart/add", "/api/orders/checkout").permitAll()

                        // Rutas de Acceso Autenticado (Usuario Logueado)
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/update").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/delete").authenticated()


                        // 👉 REGLAS DE ACCESO BASADAS EN ROL
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")

                        // 👉 FINAL: TODAS LAS DEMÁS RUTAS REQUIEREN UN TOKEN (Debe ir al final)
                        .anyRequest().authenticated()
                )
                // 5. Filtro JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}