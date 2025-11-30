package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "guest_cart")
@Data
@NoArgsConstructor
public class GuestCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificador único para el carrito del invitado (UUID)
    @Column(nullable = false)
    private String guestIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto; // Asumo que ya tienes una entidad Producto

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Instant fechaCreacion;

    // Constructor con argumentos clave
    public GuestCart(String guestIdentifier, Producto producto, Integer cantidad) {
        this.guestIdentifier = guestIdentifier;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fechaCreacion = Instant.now();
    }
}