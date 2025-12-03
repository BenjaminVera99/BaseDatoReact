package com.example.demo.controller;

import com.example.demo.dto.CheckoutRequest;
import com.example.demo.model.Pedido;
import com.example.demo.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Pedidos y Carrito", description = "Gestión del carrito de compras y finalización de pedidos")

public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Operation(summary = "Finaliza el carrito de compras y crea un nuevo pedido persistente (invitado o registrado)")
    @PostMapping("/orders/checkout")
    public ResponseEntity<?> crearPedido(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request
    ) {
        String username = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }

        try {
            Pedido pedido;

            if (username != null) {
                pedido = pedidoService.crearPedidoUsuarioRegistrado(username);

            } else if (request.getGuestIdentifier() != null && !request.getGuestIdentifier().isEmpty()) {

                if (request.getEmail() == null || request.getDireccionEnvio() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de envío son obligatorios para invitados.");
                }

                pedido = pedidoService.crearPedidoInvitado(request);

            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión o tener un carrito de invitado activo.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al finalizar el pedido: " + e.getMessage());
        }
    }


    @Operation(summary = "Obtiene el historial de pedidos del usuario autenticado")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/orders/me")
    public ResponseEntity<List<Pedido>> obtenerHistorialPedidos(Authentication authentication) {
        String username = authentication.getName();
        List<Pedido> pedidos = pedidoService.obtenerHistorialPedidos(username);
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Obtiene los detalles de un pedido específico por ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/orders/{pedidoId}")
    public ResponseEntity<Pedido> obtenerPedidoPorId(@PathVariable Long pedidoId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId, username);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}