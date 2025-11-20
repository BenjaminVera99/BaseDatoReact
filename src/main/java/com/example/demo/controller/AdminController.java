package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/panel")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel() {
        return "Solo los administradores pueden ver este panel";
    }
}
