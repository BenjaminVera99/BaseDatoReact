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

// --- NUEVOS IMPORTS NECESARIOS PARA CORS ---
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // ====================================================================================
    // ⭐ 1. NUEVO BEAN PARA CONFIGURACIÓN CORS ⭐
    // Permite que la aplicación móvil (u otros orígenes) se conecte a la API.
    // ====================================================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🚨 Configuración temporal de prueba: permite cualquier origen.
        // En producción, es mejor listar los orígenes específicos (ej. tu dominio web).
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a TODAS las rutas de la API.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ====================================================================================
    // 💡 2. FILTER CHAIN MODIFICADO (INTEGRACIÓN CORS) 💡
    // ====================================================================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ⭐ INYECTAR LA CONFIGURACIÓN CORS AQUÍ ⭐
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Deshabilita CSRF (necesario para APIs REST sin estado)
                .csrf(csrf -> csrf.disable())
                // Configura la política de sesión sin estado
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // 🔑 1. RUTAS DE AUTENTICACIÓN
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register"
                        ).permitAll()

                        // 🛒 2. RUTAS DEL CARRITO Y CHECKOUT DE INVITADO: ¡DEBEN SER PÚBLICAS!
                        // Permite todas las operaciones del carrito para invitados (POST, GET, DELETE)
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cart/add").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/checkout").permitAll()

                        // Permite la finalización de la compra (POST) para el flujo de invitado (que no lleva JWT)
                        .requestMatchers(HttpMethod.POST, "/api/orders/checkout").permitAll()


                        // 🔑 3. RUTAS PÚBLICAS Y SWAGGER
                        .requestMatchers(
                                HttpMethod.GET, // 👈 Se añadió el método GET explícitamente para /products
                                "/products",    // 👈 Se añadió la ruta exacta /products (sin wildcard)
                                "/products/**",
                                "/api/products", // 👈 Se añadió la ruta exacta /api/products
                                "/api/products/**"

                        ).permitAll()

                        // Otras rutas públicas...
                        .requestMatchers(
                                // Rutas de Swagger/OpenAPI
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/configuration/**",
                                "/webjars/**",


                                // Rutas estáticas generales
                                "/", "/*.png", "/*.jpg", "/*.jpeg", "/*.html", "/*.css", "/*.js", "/css/**", "/js/**"
                        ).permitAll()


                        // 👉 REGLAS DE ACCESO BASADAS EN ROL
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")

                        // 👉 TODAS LAS DEMÁS RUTAS REQUIEREN UN TOKEN
                        // Por ejemplo, GET /api/orders/me (historial) o GET /auth/me (perfil)
                        .anyRequest().authenticated()
                )
                // Asegura que el filtro JWT se ejecute antes del filtro de autenticación estándar
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