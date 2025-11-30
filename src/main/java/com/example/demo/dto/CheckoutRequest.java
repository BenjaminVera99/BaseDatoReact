package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO usado tanto para usuarios registrados como para invitados al finalizar la compra
@Data
public class CheckoutRequest {

    // 1. Identificador de Carrito (Clave para la lógica de bifurcación)
    // Será nulo si el usuario está autenticado, pero requerido si es invitado.
    private String guestIdentifier;

    // 2. Datos de Contacto/Envío (Requerido solo para invitados)

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El nombre de la persona que recibe es obligatorio")
    private String nombreReceptor;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefono;

    // Puedes agregar aquí métodos de pago, notas, etc.
}