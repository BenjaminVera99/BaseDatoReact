package com.example.demo.controller;

import com.example.demo.dto.CarritoItemDto; // Usaremos CarritoItemDto para agregar (si lo tienes)
import com.example.demo.dto.CartAddRequest; // Usamos el DTO original para recibir la petición
import com.example.demo.model.GuestCart;
import com.example.demo.service.PedidoService; // ¡CORRECCIÓN CLAVE! Usar PedidoService
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrito de Compras", description = "Gestión del carrito para usuarios autenticados e invitados")
public class CartController {

    @Autowired
    private PedidoService pedidoService; // ¡CORRECCIÓN CLAVE! Cambiado de ProductoService a PedidoService

    // --- Método auxiliar para determinar el contexto ---
    private String getAuthenticatedUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // 1. POST /api/cart/add (Agregar/Actualizar Producto)
    // ----------------------------------------------------------------------

    @Operation(summary = "Agrega un producto al carrito del usuario (registrado o invitado)")
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addProductToCart(
            Authentication authentication,
            @Valid @RequestBody CartAddRequest request
    ) {
        String username = getAuthenticatedUsername(authentication);
        String guestId = request.getGuestIdentifier();

        if (username != null) {
            // CONTEXTO: USUARIO AUTENTICADO
            // Creamos el DTO interno para el método del servicio
            CarritoItemDto itemDto = new CarritoItemDto();
            itemDto.setProductoId(request.getProductId());
            itemDto.setCantidad(request.getCantidad());

            // Llamada correcta al PedidoService
            pedidoService.agregarItem(username, itemDto);
            return ResponseEntity.ok(Map.of("message", "Producto agregado al carrito de usuario."));

        } else {
            // CONTEXTO: INVITADO
            String targetGuestId;

            if (guestId == null || guestId.isEmpty()) {
                // Caso A: Primera interacción. Generar nuevo guest ID.
                targetGuestId = UUID.randomUUID().toString();
            } else {
                // Caso B: Usar ID existente.
                targetGuestId = guestId;
            }

            // Llamada correcta al PedidoService
            pedidoService.addToGuestCart(targetGuestId, request.getProductId(), request.getCantidad());

            if (guestId == null || guestId.isEmpty()) {
                // Devolver el nuevo ID para que el frontend lo almacene
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        Map.of("message", "Carrito de invitado creado. Producto agregado.", "guestIdentifier", targetGuestId)
                );
            }
            return ResponseEntity.ok(Map.of("message", "Producto agregado al carrito de invitado."));
        }
    }

    // ----------------------------------------------------------------------
    // 2. GET /api/cart (Ver Contenido)
    // ----------------------------------------------------------------------

    @Operation(summary = "Ver el contenido actual del carrito")
    @GetMapping
    public ResponseEntity<?> verCarrito(
            Authentication authentication,
            @RequestParam(required = false) String guestIdentifier
    ) {
        String username = getAuthenticatedUsername(authentication);

        if (username != null) {
            // CONTEXTO: USUARIO AUTENTICADO
            // Llamada correcta al PedidoService
            return ResponseEntity.ok(pedidoService.verCarrito(username));
        } else if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
            // CONTEXTO: INVITADO
            // Llamada correcta al PedidoService
            List<GuestCart> carritoInvitado = pedidoService.getGuestCart(guestIdentifier);
            return ResponseEntity.ok(carritoInvitado);
        } else {
            // No hay autenticación ni identificador de invitado
            return ResponseEntity.ok(Map.of("message", "Carrito vacío o no identificado."));
        }
    }

    // ----------------------------------------------------------------------
    // 3. DELETE /api/cart/{productId} (Eliminar Ítem)
    // ----------------------------------------------------------------------

    @Operation(summary = "Elimina un producto específico del carrito")
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> eliminarItemDelCarrito(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam(required = false) String guestIdentifier
    ) {
        String username = getAuthenticatedUsername(authentication);

        if (username != null) {
            // CONTEXTO: USUARIO AUTENTICADO
            // Llamada correcta al PedidoService
            pedidoService.eliminarItem(username, productId);
            return ResponseEntity.ok("Producto eliminado del carrito de usuario.");
        } else if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
            // CONTEXTO: INVITADO
            // Llamada correcta al PedidoService
            pedidoService.deleteGuestCartItem(guestIdentifier, productId);
            return ResponseEntity.ok("Producto eliminado del carrito de invitado.");
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito no identificado.");
        }
    }

    // ----------------------------------------------------------------------
    // 4. DELETE /api/cart/clear (Vaciar Carrito)
    // ----------------------------------------------------------------------

    @Operation(summary = "Vacía completamente el carrito")
    @DeleteMapping("/clear")
    public ResponseEntity<String> limpiarCarrito(
            Authentication authentication,
            @RequestParam(required = false) String guestIdentifier
    ) {
        String username = getAuthenticatedUsername(authentication);

        if (username != null) {
            // CONTEXTO: USUARIO AUTENTICADO
            // Llamada correcta al PedidoService
            pedidoService.limpiarCarrito(username);
            return ResponseEntity.ok("Carrito de usuario vaciado con éxito.");
        } else if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
            // CONTEXTO: INVITADO
            // Llamada correcta al PedidoService
            pedidoService.clearGuestCart(guestIdentifier);
            return ResponseEntity.ok("Carrito de invitado vaciado con éxito.");
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito no identificado.");
        }
    }
}