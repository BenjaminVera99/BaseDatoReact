package com.example.demo.controller;

import com.example.demo.dto.CarritoItemDto;
import com.example.demo.dto.CartAddRequest;
import com.example.demo.model.GuestCart;
import com.example.demo.service.PedidoService;
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
    private PedidoService pedidoService;

    // --- Método auxiliar para determinar el contexto ---
    private String getAuthenticatedUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }



    @Operation(summary = "Agrega un producto al carrito del usuario (registrado o invitado)")
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addProductToCart(
            Authentication authentication,
            @Valid @RequestBody CartAddRequest request
    ) {
        String username = getAuthenticatedUsername(authentication);
        String guestId = request.getGuestIdentifier();

        if (username != null) {
            CarritoItemDto itemDto = new CarritoItemDto();
            itemDto.setProductoId(request.getProductId());
            itemDto.setCantidad(request.getCantidad());

            pedidoService.agregarItem(username, itemDto);
            return ResponseEntity.ok(Map.of("message", "Producto agregado al carrito de usuario."));

        } else {
            String targetGuestId;

            if (guestId == null || guestId.isEmpty()) {
                targetGuestId = UUID.randomUUID().toString();
            } else {
                targetGuestId = guestId;
            }

            pedidoService.addToGuestCart(targetGuestId, request.getProductId(), request.getCantidad());

            if (guestId == null || guestId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        Map.of("message", "Carrito de invitado creado. Producto agregado.", "guestIdentifier", targetGuestId)
                );
            }
            return ResponseEntity.ok(Map.of("message", "Producto agregado al carrito de invitado."));
        }
    }



    @Operation(summary = "Ver el contenido actual del carrito")
    @GetMapping
    public ResponseEntity<?> verCarrito(
            Authentication authentication,
            @RequestParam(required = false) String guestIdentifier
    ) {
        String username = getAuthenticatedUsername(authentication);

        if (username != null) {
            return ResponseEntity.ok(pedidoService.verCarrito(username));
        } else if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
            List<GuestCart> carritoInvitado = pedidoService.getGuestCart(guestIdentifier);
            return ResponseEntity.ok(carritoInvitado);
        } else {
            return ResponseEntity.ok(Map.of("message", "Carrito vacío o no identificado."));
        }
    }



    @Operation(summary = "Elimina un producto específico del carrito")
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> eliminarItemDelCarrito(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam(required = false) String guestIdentifier
    ) {
        String username = getAuthenticatedUsername(authentication);

        if (username != null) {
            pedidoService.eliminarItem(username, productId);
            return ResponseEntity.ok("Producto eliminado del carrito de usuario.");
        } else if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
            pedidoService.deleteGuestCartItem(guestIdentifier, productId);
            return ResponseEntity.ok("Producto eliminado del carrito de invitado.");
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito no identificado.");
        }
    }



    @Operation(summary = "Vacía completamente el carrito")
    @DeleteMapping("/clear")
    public ResponseEntity<String> limpiarCarrito(
            Authentication authentication,
            @RequestParam(required = false) String guestIdentifier
    ) {
        String username = getAuthenticatedUsername(authentication);

        if (username != null) {
            pedidoService.limpiarCarrito(username);
            return ResponseEntity.ok("Carrito de usuario vaciado con éxito.");
        } else if (guestIdentifier != null && !guestIdentifier.isEmpty()) {
            pedidoService.clearGuestCart(guestIdentifier);
            return ResponseEntity.ok("Carrito de invitado vaciado con éxito.");
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito no identificado.");
        }
    }
}