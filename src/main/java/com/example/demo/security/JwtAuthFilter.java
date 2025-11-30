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
// Las importaciones de RequestMatcher, RegexRequestMatcher, shouldNotFilter, etc. ya no son necesarias.
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUsuarioDetailsService usuarioDetailsService;

    // ❌ IMPORTANTE: El método shouldNotFilter y la lista publicMatchers se ELIMINAN.
    // La responsabilidad de permitir el acceso público recae ÚNICAMENTE en SecurityConfig.

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ⭐ 1. Permitir preflight CORS (No cambia)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // ⭐ 2. CAMBIO CLAVE: Si no hay token, simplemente DEJAMOS CONTINUAR la cadena.
        // Spring Security revisará la configuración y, si la ruta es permitAll(), dejará pasar la solicitud.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Detenemos la lógica del JWT y pasamos el control al siguiente filtro.
        }

        // Si SÍ hay token, procedemos a validarlo.
        String token = authHeader.substring(7);
        String username = null;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            // ⭐ CAMBIO CLAVE: Lanza la excepción para que Spring la intercepte. ⭐
            // Esto asegura que Spring Security pueda manejar la expiración o invalidez del JWT.
            // Usamos 'request.setAttribute' para guardar la excepción si queremos un manejo más fino en otra clase.
            // Pero la forma más simple es re-lanzar o dejar que falle.

            // Por simplicidad y para un manejo correcto por parte de Spring Security:
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido o expirado");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails.getUsername())) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}