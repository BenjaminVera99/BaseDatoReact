package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. AJUSTE CLAVE: Hacer la clave foránea del usuario opcional (nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true) // <--- CAMBIO AQUÍ
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false)
    private Double total = 0.0;

    // 2. AJUSTE: Campos para guardar la información del receptor (invitado o registrado)
    // Cuando 'usuario' es NULL, estos campos son obligatorios.
    @Column(nullable = true)
    private String emailReceptor;

    @Column(nullable = true)
    private String nombreReceptor;

    @Column(nullable = true)
    private String direccionEnvio;

    @Column(nullable = true)
    private String telefono;


    // Relación Uno a Muchos con DetallePedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles;

    public Pedido(Usuario usuario) {
        this.usuario = usuario;
    }
}

enum EstadoPedido {
    PENDIENTE,
    PROCESANDO,
    ENVIADO,
    ENTREGADO,
    CANCELADO
}