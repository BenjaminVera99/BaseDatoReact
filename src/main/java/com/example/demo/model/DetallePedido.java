package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalles_pedido")
@Data
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Muchos a Uno con Pedido
    // Esto es el lado dueño de la relación con Pedido.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Relación Muchos a Uno con Producto
    // Mantenemos la referencia al producto, pero no su precio actual.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    // El precio debe ser almacenado en el detalle
    // para que el historial de pedidos no cambie si el precio del producto cambia.
    @Column(nullable = false)
    private Double precioUnitario;

    // Constructor para inicializar con valores clave
    public DetallePedido(Pedido pedido, Producto producto, Integer cantidad, Double precioUnitario) {
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
}