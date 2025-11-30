package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

// DTO usado para solicitudes de agregar o actualizar productos en el carrito
@Data
public class CartAddRequest {

    @NotNull(message = "El ID del producto no puede ser nulo")
    private Long productId; // Cambiado a 'productId' para consistencia con el DTO

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    private Integer cantidad;

    // Campo adicional para el identificador del carrito del invitado.
    // Es nulo si el usuario está autenticado, o contiene el ID temporal si es un invitado.
    private String guestIdentifier;
}