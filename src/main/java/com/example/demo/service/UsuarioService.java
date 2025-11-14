package com.example.demo.service;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario register(String username, String password) {
        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
}
