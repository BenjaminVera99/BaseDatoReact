package com.example.demo.controller;

import com.example.demo.security.JwtService;
import com.example.demo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Usuarios", description = "Gestión de Usuarios de la Pastelería")
public class UsuarioAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    // REGISTRO DE USUARIO
    @PostMapping("/register")
    @Operation(summary = "Registro de Usuarios")
    public Map<String, String> register(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");
        String nombres = body.get("nombres");
        String apellidos = body.get("apellidos");
        String fechaNac = body.get("fechaNac");

        try {
            usuarioService.register(username, password, nombres, apellidos, fechaNac);
            return Map.of("message", "Usuario registrado correctamente");
        } catch (RuntimeException e) {
            return Map.of("error", e.getMessage());
        }
    }

    // LOGIN DE USUARIO - DEVUELVE TOKEN JWT
    @PostMapping("/login")
    @Operation(summary = "Inicio de Sesion Usuarios")
    public Map<String, String> login(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            if (auth.isAuthenticated()) {

                String role = auth.getAuthorities().iterator().next().getAuthority();

                String token = jwtService.generateToken(username, role);

                return Map.of(
                        "token", token,
                        "role", role
                );
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
    }

    // OBTENER PERFIL DEL USUARIO
    @GetMapping("/me")
    @Operation(summary = "Identificar Usuario Logeado")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.extractUsername(token);

        var userOpt = usuarioService.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        var user = userOpt.get();

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nombres", user.getNombres(),
                "apellidos", user.getApellidos(),
                "fechaNac", user.getFechaNac(),
                "role", user.getRole()
        );
    }
}
