package com.example.demo.service;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(String username, String password) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));

        usuario.setRole("USER"); // 👈 rol por defecto

        usuarioRepository.save(usuario);
    }

    // ⭐ NUEVO: obtener rol desde la base de datos
    public String getRoleByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(Usuario::getRole)
                .orElse("USER");
    }
}
