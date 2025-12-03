package com.example.demo.service;

import com.example.demo.dto.Registro;
import com.example.demo.dto.UsuarioUpdateDto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(Registro registro) {

        if (usuarioRepository.findByUsername(registro.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(registro.getUsername());
        usuario.setPassword(passwordEncoder.encode(registro.getPassword()));

        usuario.setNombres(registro.getNombres());
        usuario.setApellidos(registro.getApellidos());
        usuario.setFechaNac(registro.getFechaNac());
        usuario.setDireccion(registro.getDireccion());

        if (registro.getUsername().endsWith("@admin.com")) {
            usuario.setRole("ROLE_ADMIN");
        } else {
            usuario.setRole("ROLE_USER");
        }

        usuarioRepository.save(usuario);
    }

    public Usuario updateProfile(String currentUsername, UsuarioUpdateDto updateData) {

        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setNombres(updateData.getNombre());
        usuario.setApellidos(updateData.getApellidos());
        usuario.setFechaNac(updateData.getFechaNac());
        usuario.setDireccion(updateData.getDireccion());
        usuario.setProfilePictureUri(updateData.getProfilePictureUri());

        if (!currentUsername.equals(updateData.getCorreo())) {
            if (usuarioRepository.findByUsername(updateData.getCorreo()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nuevo correo ya está en uso.");
            }
            usuario.setUsername(updateData.getCorreo());
        }


        String newPassword = updateData.getContrasena();
        if (newPassword != null && !newPassword.isEmpty()) {
            String hashedPassword = passwordEncoder.encode(newPassword);
            usuario.setPassword(hashedPassword);
        }

        return usuarioRepository.save(usuario);
    }

    public void deleteUser(String username) {
        var userOpt = usuarioRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
        usuarioRepository.delete(userOpt.get());
    }



    // ⭐ NECESARIO PARA /auth/me
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public String getRoleByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(Usuario::getRole)
                .orElse("USER");
    }

    public List<Usuario> findAllUsers() {
        return usuarioRepository.findAll();
    }
}
