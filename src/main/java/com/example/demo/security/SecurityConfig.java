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
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // Permite todos los métodos (GET, POST, etc.)
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

                // 2. CSRF (Deshabilitado)
                .csrf(csrf -> csrf.disable())

                // 3. Sesión (Sin estado)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Autorización de Peticiones
                .authorizeHttpRequests(auth -> auth

                        // 🔑 Rutas de Autenticación (Públicas)
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register"
                        ).permitAll()

                        // 🛒 Rutas de Carrito (Públicas)
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cart/add", "/api/orders/checkout").permitAll()

                        // ⭐ RUTAS DE PRODUCTOS: Aseguramos que solo el GET sea público para evitar que POST/PUT se hagan sin token si no están en otra parte del código.
                        .requestMatchers(HttpMethod.GET,
                                "/products",
                                "/products/**",
                                "/api/products",
                                "/api/products/**"
                        ).permitAll()

                        // Rutas Estáticas y Swagger (Públicas)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/", "/*.png", "/*.jpg", "/*.jpeg", "/*.html", "/*.css", "/*.js", "/css/**", "/js/**"
                        ).permitAll()


                        // 👉 REGLAS DE ACCESO BASADAS EN ROL
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")

                        // 👉 TODAS LAS DEMÁS RUTAS REQUIEREN UN TOKEN
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