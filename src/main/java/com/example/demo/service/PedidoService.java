package com.example.demo.service;

import com.example.demo.dto.CarritoItemDto;
import com.example.demo.dto.CheckoutRequest;
import com.example.demo.model.*;
import com.example.demo.repository.DetallePedidoRepository;
import com.example.demo.repository.GuestCartRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GuestCartRepository guestCartRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    private final Map<Long, Map<Long, Integer>> carritosActivos = new HashMap<>();

    private Long getUsuarioId(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
        return usuario.getId();
    }

    public void agregarItem(String username, CarritoItemDto itemDto) {
        Long userId = getUsuarioId(username);
        if (!productoRepository.existsById(itemDto.getProductoId())) {
            throw new IllegalArgumentException("Producto con ID " + itemDto.getProductoId() + " no encontrado.");
        }
        Map<Long, Integer> carrito = carritosActivos.computeIfAbsent(userId, k -> new HashMap<>());
        carrito.merge(itemDto.getProductoId(), itemDto.getCantidad(), Integer::sum);
    }


    public Map<Producto, Integer> verCarrito(String username) {
        Long userId = getUsuarioId(username);
        Map<Long, Integer> carritoIds = carritosActivos.getOrDefault(userId, new HashMap<>());
        Map<Producto, Integer> carritoCompleto = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : carritoIds.entrySet()) {
            Optional<Producto> producto = productoRepository.findById(entry.getKey());
            producto.ifPresent(p -> carritoCompleto.put(p, entry.getValue()));
        }
        return carritoCompleto;
    }


    public void eliminarItem(String username, Long productoId) {
        Long userId = getUsuarioId(username);
        Map<Long, Integer> carrito = carritosActivos.get(userId);
        if (carrito != null) {
            carrito.remove(productoId);
            if (carrito.isEmpty()) {
                carritosActivos.remove(userId);
            }
        }
    }


    public void limpiarCarrito(String username) {
        Long userId = getUsuarioId(username);
        carritosActivos.remove(userId);
    }


    @Transactional
    public void addToGuestCart(String guestIdentifier, Long productId, Integer cantidad) {
        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + productId));

        Optional<GuestCart> existingItem =
                guestCartRepository.findByGuestIdentifierAndProductoId(guestIdentifier, productId);

        if (existingItem.isPresent()) {
            GuestCart item = existingItem.get();
            item.setCantidad(item.getCantidad() + cantidad);
            guestCartRepository.save(item);
        } else {
            GuestCart newItem = new GuestCart(guestIdentifier, producto, cantidad);
            guestCartRepository.save(newItem);
        }
    }


    public List<GuestCart> getGuestCart(String guestIdentifier) {
        if (guestIdentifier == null || guestIdentifier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de invitado es requerido.");
        }
        return guestCartRepository.findByGuestIdentifier(guestIdentifier);
    }


    @Transactional
    public void deleteGuestCartItem(String guestIdentifier, Long productId) {
        guestCartRepository.deleteByGuestIdentifierAndProductoId(guestIdentifier, productId);
    }


    @Transactional
    public void clearGuestCart(String guestIdentifier) {
        guestCartRepository.deleteByGuestIdentifier(guestIdentifier);
    }



    @Transactional
    public Pedido crearPedidoUsuarioRegistrado(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        Long userId = usuario.getId();
        Map<Long, Integer> carritoIds = carritosActivos.get(userId);

        if (carritoIds == null || carritoIds.isEmpty()) {
            throw new IllegalStateException("El carrito del usuario está vacío.");
        }

        Pedido nuevoPedido = new Pedido(usuario);
        nuevoPedido.setFechaCreacion(LocalDateTime.now());

        nuevoPedido.setEmailReceptor(usuario.getUsername());

        List<DetallePedido> detalles = new ArrayList<>();
        double totalPedido = 0.0;

        for (Map.Entry<Long, Integer> item : carritoIds.entrySet()) {
            Producto producto = productoRepository.findById(item.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Producto ID " + item.getKey() + " ya no existe."));

            Integer cantidad = item.getValue();
            Double precio = producto.getPrice();
            double subtotal = precio * cantidad;
            totalPedido += subtotal;

            DetallePedido detalle = new DetallePedido(nuevoPedido, producto, cantidad, precio);
            detalles.add(detalle);
        }

        nuevoPedido.setTotal(totalPedido);
        nuevoPedido.setDetalles(detalles);

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        limpiarCarrito(username);

        return pedidoGuardado;
    }


    @Transactional
    public Pedido crearPedidoInvitado(CheckoutRequest request) {

        String guestIdentifier = request.getGuestIdentifier();

        List<GuestCart> cartItems = guestCartRepository.findByGuestIdentifier(guestIdentifier);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito de invitado está vacío.");
        }

        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuario(null);
        nuevoPedido.setFechaCreacion(LocalDateTime.now());

        nuevoPedido.setEmailReceptor(request.getEmail());
        nuevoPedido.setNombreReceptor(request.getNombreReceptor());
        nuevoPedido.setDireccionEnvio(request.getDireccionEnvio());
        nuevoPedido.setTelefono(request.getTelefono());

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        double total = 0.0;

        for (GuestCart item : cartItems) {
            Producto producto = item.getProducto();
            Double precio = producto.getPrice();

            DetallePedido detalle = new DetallePedido(
                    pedidoGuardado,
                    producto,
                    item.getCantidad(),
                    precio
            );
            detallePedidoRepository.save(detalle);
            total += item.getCantidad() * precio;
        }

        pedidoGuardado.setTotal(total);
        pedidoRepository.save(pedidoGuardado);

        clearGuestCart(guestIdentifier);

        return pedidoGuardado;
    }


    @Transactional
    public void fusionarCarrito(String username, String guestIdentifier) {
        if (guestIdentifier == null || guestIdentifier.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        Long userId = usuario.getId();

        List<GuestCart> guestItems = guestCartRepository.findByGuestIdentifier(guestIdentifier);

        if (guestItems.isEmpty()) {
            guestCartRepository.deleteByGuestIdentifier(guestIdentifier);
            return;
        }

        Map<Long, Integer> userCart = carritosActivos.computeIfAbsent(userId, k -> new HashMap<>());

        for (GuestCart guestItem : guestItems) {
            Long productId = guestItem.getProducto().getId();
            Integer cantidadInvitado = guestItem.getCantidad();

            userCart.merge(productId, cantidadInvitado, Integer::sum);
        }

        guestCartRepository.deleteByGuestIdentifier(guestIdentifier);
    }

    public List<Pedido> obtenerHistorialPedidos(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        return pedidoRepository.findByUsuario(usuario);
    }


    public Pedido obtenerPedidoPorId(Long pedidoId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        return pedidoRepository.findById(pedidoId)
                .filter(p -> p.getUsuario() == null || p.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado o no pertenece al usuario."));
    }
}