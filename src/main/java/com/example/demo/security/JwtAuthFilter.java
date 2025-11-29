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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// 🔑 NUEVAS IMPORTACIONES
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUsuarioDetailsService usuarioDetailsService;

    // 🔑 1. DEFINIR LAS RUTAS A EXCLUIR USANDO RequestMatcher
    private final List<RequestMatcher> publicMatchers = Arrays.asList(
            // Login y Registro
            new AntPathRequestMatcher("/auth/**"),
            // Rutas de productos (móvil y web)
            new AntPathRequestMatcher("/products/**"),
            new AntPathRequestMatcher("/api/products/**")
    );

    // 🔑 2. USAR shouldNotFilter PARA IGNORAR EL FILTRO EN RUTAS PÚBLICAS
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Verifica si la URI de la petición actual coincide con alguna de las rutas públicas
        return publicMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ⭐ Permitir preflight CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ⚠️ La lógica de exclusión se mueve a shouldNotFilter, por lo que aquí ya no está.

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Ya que shouldNotFilter asegura que solo llegamos aquí si hay token o si es una ruta protegida
            // En una ruta protegida sin token, lanzamos el error
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Cambiado a 401
            return;
        }

        String token = authHeader.substring(7);

        String username = null;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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