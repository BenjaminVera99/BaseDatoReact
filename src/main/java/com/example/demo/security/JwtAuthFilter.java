package com.example.demo.security;

import com.example.demo.service.CustomUsuarioDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUsuarioDetailsService usuarioDetailsService;

    // 🔑 1. DEFINIR LAS RUTAS A EXCLUIR USANDO RequestMatcher (LISTA CORREGIDA)
    private final List<RequestMatcher> publicMatchers = Arrays.asList(
            // 🔑 USAMOS RegexRequestMatcher con patrones simples

            // Login y Registro: RegexRequestMatcher usa String para el método, lo que resuelve el conflicto
            new RegexRequestMatcher("/auth/login", "POST"),
            new RegexRequestMatcher("/auth/register", "POST"),

            // Rutas de productos (sin método)
            new RegexRequestMatcher("/products/.*", null), // El '.*' es el equivalente regex de '/**'
            new RegexRequestMatcher("/api/products/.*", null),

            // Rutas de Swagger/OpenAPI (sin método)
            new RegexRequestMatcher("/v3/api-docs", null),
            new RegexRequestMatcher("/v3/api-docs/.*", null),
            new RegexRequestMatcher("/swagger-ui/.*", null),
            new RegexRequestMatcher("/swagger-resources/.*", null),
            new RegexRequestMatcher("/webjars/.*", null),
            new RegexRequestMatcher("/configuration/.*", null),

            // Rutas estáticas generales (sin método)
            new RegexRequestMatcher("/", null),
            new RegexRequestMatcher("/.*\\.png", null),
            new RegexRequestMatcher("/.*\\.jpg", null),
            new RegexRequestMatcher("/.*\\.jpeg", null),
            new RegexRequestMatcher("/.*\\.html", null),
            new RegexRequestMatcher("/.*\\.css", null),
            new RegexRequestMatcher("/.*\\.js", null),
            new RegexRequestMatcher("/css/.*", null),
            new RegexRequestMatcher("/js/.*", null)
    );

    // 🔑 2. USAR shouldNotFilter PARA IGNORAR EL FILTRO EN RUTAS PÚBLICAS
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Verifica si la URI de la petición actual coincide con alguna de las rutas públicas
        // Si coincide, devuelve true, e IGNORA el filtro.
        return publicMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ⭐ Permitir preflight CORS (No cambia)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si no hay token y la ruta no fue ignorada por shouldNotFilter, es una ruta protegida.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            return;
        }

        String token = authHeader.substring(7);

        String username = null;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 (Token inválido o expirado)
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(username);

            // ⭐ Validar token
            if (!jwtService.validateToken(token, userDetails.getUsername())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}