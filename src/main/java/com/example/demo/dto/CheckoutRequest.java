package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {

    private String guestIdentifier;


    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El nombre de la persona que recibe es obligatorio")
    private String nombreReceptor;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefono;

}