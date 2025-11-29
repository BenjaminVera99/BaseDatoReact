package com.example.demo.controller;

import com.example.demo.dto.InicioSesion;
import com.example.demo.dto.Registro;
import com.example.demo.model.Usuario;
import com.example.demo.security.JwtService;
import com.example.demo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
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
    // ⚠️ CAMBIO: El método register en el servicio debe recibir el DTO completo
    // y la lógica de validación se mueve al servicio (como ya lo definimos).
    public Map<String, String> register(@RequestBody Registro registro) {

        try {
            // Llama al nuevo método en el servicio que acepta el DTO 'Registro'
            usuarioService.register(registro);
            return Map.of("message", "Usuario registrado correctamente");
        } catch (ResponseStatusException e) {
            // Captura las excepciones específicas lanzadas desde el servicio (ej. 409 Conflict)
            throw e;
        } catch (Exception e) {
            // Manejo de errores genéricos (e.g., error de DB)
            return Map.of("error", e.getMessage());
        }
    }

    // LOGIN DE USUARIO - DEVUELVE TOKEN JWT
    @PostMapping("/login")
    @Operation(summary = "Inicio de Sesion Usuarios")
    public Map<String, String> login(@RequestBody InicioSesion inicioSesion) {

        String username = inicioSesion.getUsername();
        String password = inicioSesion.getPassword();

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

    @PutMapping("/update")
    @Operation(summary = "Actualizar datos completos del perfil de usuario")
    // Asegúrate de importar UsuarioUpdateDto
    public Map<String, Object> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody com.example.demo.dto.UsuarioUpdateDto updateData) // Asegúrate de usar el DTO correcto
    {
        // 1. Extraer el nombre de usuario (correo) del token JWT
        String token = authHeader.replace("Bearer ", "");
        String currentUsername = jwtService.extractUsername(token);

        try {
            // 2. Usar el servicio para actualizar la lógica crítica
            Usuario updatedUser = usuarioService.updateProfile(currentUsername, updateData);

            // 3. Devolver la información actualizada (sin la contraseña, pero con el rol)
            return Map.of(
                    "message", "Perfil actualizado correctamente.",
                    "nombre", updatedUser.getNombres(),
                    "apellidos", updatedUser.getApellidos(),
                    "correo", updatedUser.getUsername(), // El nuevo correo/username
                    "fechaNac", updatedUser.getFechaNac(),
                    "direccion", updatedUser.getDireccion(),
                    "profilePictureUri", updatedUser.getProfilePictureUri() != null ? updatedUser.getProfilePictureUri() : "",
                    "role", updatedUser.getRole()
            );

        } catch (ResponseStatusException e) {
            // Maneja la excepción lanzada desde el servicio (ej. Correo ya en uso)
            throw e;
        } catch (Exception e) {
            // Maneja cualquier otra excepción inesperada
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la actualización.");
        }
    }
}
