package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDto {
    private String nombre;
    private String apellidos;
    private String correo;
    private String contrasena;
    private String fechaNac;
    private String direccion;
    private String profilePictureUri;
}