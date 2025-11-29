package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDto {
    private String nombre; // Mapea a 'nombres' en la entidad
    private String apellidos;
    private String correo; // Mapea a 'username'
    private String contrasena; // Mapea a 'password'
    private String fechaNac;
    private String direccion;
    private String profilePictureUri;
}