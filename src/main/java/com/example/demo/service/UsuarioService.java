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

        // 1. Buscar el usuario actual usando el username (correo) del token
        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 2. Aplicar los cambios de campos simples (mapeando del DTO a la Entidad)
        usuario.setNombres(updateData.getNombre());
        usuario.setApellidos(updateData.getApellidos());
        usuario.setFechaNac(updateData.getFechaNac());
        usuario.setDireccion(updateData.getDireccion());
        usuario.setProfilePictureUri(updateData.getProfilePictureUri());

        // 3. CAMBIO CRÍTICO: Actualización de Correo (Username)
        if (!currentUsername.equals(updateData.getCorreo())) {
            // Valida si el nuevo correo ya está en uso
            if (usuarioRepository.findByUsername(updateData.getCorreo()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nuevo correo ya está en uso.");
            }
            // Si es único, actualiza el username
            usuario.setUsername(updateData.getCorreo());
        }

        // 4. CAMBIO CRÍTICO: Actualización de Contraseña
        // Solo hashea y actualiza si el usuario envió una nueva contraseña
        String newPassword = updateData.getContrasena();
        if (newPassword != null && !newPassword.isEmpty()) {
            String hashedPassword = passwordEncoder.encode(newPassword);
            usuario.setPassword(hashedPassword);
        }

        // 5. Guardar el usuario actualizado en la base de datos
        return usuarioRepository.save(usuario);
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
