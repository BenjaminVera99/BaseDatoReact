package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Gestión de Administracion de la Pastelería")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/panel")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel() {
        return "Solo los administradores pueden ver este panel";
    }

    @GetMapping("all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver una lista de Usuarios Registrados")
    public List<Usuario> findAllUsers() {
        return usuarioService.findAllUsers();
    }
}