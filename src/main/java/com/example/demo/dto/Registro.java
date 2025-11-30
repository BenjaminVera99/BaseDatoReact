package com.example.demo.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registro {
    private String username;
    private String password;
    private String nombres;
    private String apellidos;
    private String fechaNac;
    private String direccion;
    private String guestIdentifier;
}