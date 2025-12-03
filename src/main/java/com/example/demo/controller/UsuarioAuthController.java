package com.example.demo.controller;

import com.example.demo.dto.InicioSesion;
import com.example.demo.dto.Registro;
import com.example.demo.model.Usuario;
import com.example.demo.security.JwtService;
import com.example.demo.service.PedidoService;
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
@RequestMapping("/api/auth")
@Tag(name = "Usuarios", description = "Gestión de Usuarios de la Pastelería")
public class UsuarioAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;


    @PostMapping("/register")
    @Operation(summary = "Registro de Usuarios")
    public Map<String, String> register(@RequestBody Registro registro) {

        try {
            usuarioService.register(registro);

            String username = registro.getUsername();
            String guestIdentifier = registro.getGuestIdentifier();

            if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
                try {
                    pedidoService.fusionarCarrito(username, guestIdentifier);
                    System.out.println("LOG: Carrito de invitado " + guestIdentifier + " fusionado tras el registro.");
                } catch (Exception fusionError) {
                    System.err.println("Error al fusionar carrito después del registro: " + fusionError.getMessage());
                }
            }

            return Map.of("message", "Usuario registrado correctamente");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }


    @PostMapping("/login")
    @Operation(summary = "Inicio de Sesion Usuarios")
    public Map<String, String> login(@RequestBody InicioSesion inicioSesion) {

        String username = inicioSesion.getUsername();
        String password = inicioSesion.getPassword();
        String guestIdentifier = inicioSesion.getGuestIdentifier();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            if (auth.isAuthenticated()) {

                if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
                    try {
                        pedidoService.fusionarCarrito(username, guestIdentifier);
                        System.out.println("LOG: Carrito de invitado " + guestIdentifier + " fusionado tras el login.");
                    } catch (Exception fusionError) {
                        // El login fue exitoso. La fusión falló, solo logueamos el error.
                        System.err.println("Error al fusionar carrito después del login: " + fusionError.getMessage());
                    }
                }

                // 3. Generar y Devolver Token JWT
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
                "direccion", user.getDireccion(),
                "role", user.getRole()
        );
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar datos completos del perfil de usuario")
    public Map<String, Object> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody com.example.demo.dto.UsuarioUpdateDto updateData)
    {
        String token = authHeader.replace("Bearer ", "");
        String currentUsername = jwtService.extractUsername(token);

        try {
            Usuario updatedUser = usuarioService.updateProfile(currentUsername, updateData);

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
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la actualización.");
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Eliminar el perfil del usuario autenticado")
    public Map<String, String> deleteProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String currentUsername = jwtService.extractUsername(token);

        try {
            usuarioService.deleteUser(currentUsername);

            return Map.of("message", "Usuario " + currentUsername + " eliminado correctamente.");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la eliminación.");
        }
    }
}