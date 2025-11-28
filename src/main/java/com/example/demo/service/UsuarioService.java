package com.example.demo.service;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(String username, String password, String nombres, String apellidos, String fechaNac) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));

        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setFechaNac(fechaNac);

        if (username.endsWith("@admin.com")) {
            usuario.setRole("ADMIN");
        } else {
            usuario.setRole("USER");
        }

        usuarioRepository.save(usuario);
    }

    // ⭐ NECESARIO PARA /auth/me
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // ⭐ Obtener rol (opcional)
    public String getRoleByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(Usuario::getRole)
                .orElse("USER");
    }

    public List<Usuario> findAllUsers() {
        return usuarioRepository.findAll();
    }
}
