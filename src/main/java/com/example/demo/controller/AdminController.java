package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.service.UsuarioService; // <-- ¡Necesitas importar el servicio!
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired; // <-- Necesitas Autowired
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Gestión de Administracion de la Pastelería")
public class AdminController {

    // ⭐ 1. DECLARACIÓN E INYECCIÓN DE LA DEPENDENCIA
    @Autowired
    private UsuarioService usuarioService;

    // Métodos de prueba (GET /admin/panel)
    @GetMapping("/panel")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel() {
        return "Solo los administradores pueden ver este panel";
    }

    // Método para obtener todos los usuarios (GET /admin/all)
    @GetMapping("all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver una lista de Usuarios Registrados")
    public List<Usuario> findAllUsers() {
        // ⭐ 2. Ahora, 'usuarioService' existe y se puede llamar.
        return usuarioService.findAllUsers();
    }
}