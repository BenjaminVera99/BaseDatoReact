package com.example.demo.service;

import com.example.demo.dto.CarritoItemDto;
import com.example.demo.dto.CheckoutRequest; // IMPORT NECESARIO
import com.example.demo.model.*;
import com.example.demo.repository.DetallePedidoRepository; // IMPORT NECESARIO
import com.example.demo.repository.GuestCartRepository; // IMPORT NECESARIO
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // IMPORT NECESARIO
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException; // IMPORT NECESARIO

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
    private GuestCartRepository guestCartRepository; // Repositorio para carritos de invitados

    @Autowired
    private DetallePedidoRepository detallePedidoRepository; // Repositorio para guardar detalles

    // ----------------------------------------------------------------------------------
    // ESTRUCTURA DE CARRO TEMPORAL (USUARIO REGISTRADO)
    // ----------------------------------------------------------------------------------
    private final Map<Long, Map<Long, Integer>> carritosActivos = new HashMap<>();

    /**
     * Obtiene el ID del usuario por su nombre de usuario (email).
     */
    private Long getUsuarioId(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
        return usuario.getId();
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DEL CARRITO (TEMPORAL - REGISTRADO)
    // ----------------------------------------------------------------------------------

    /**
     * Añade un producto al carrito del usuario, actualizando la cantidad si ya existe.
     */
    public void agregarItem(String username, CarritoItemDto itemDto) {
        Long userId = getUsuarioId(username);
        if (!productoRepository.existsById(itemDto.getProductoId())) {
            throw new IllegalArgumentException("Producto con ID " + itemDto.getProductoId() + " no encontrado.");
        }
        Map<Long, Integer> carrito = carritosActivos.computeIfAbsent(userId, k -> new HashMap<>());
        carrito.merge(itemDto.getProductoId(), itemDto.getCantidad(), Integer::sum);
    }

    /**
     * Obtiene el contenido del carrito del usuario (Producto y Cantidad).
     */
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

    /**
     * Elimina completamente un producto del carrito.
     */
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

    /**
     * Limpia completamente el carrito del usuario.
     */
    public void limpiarCarrito(String username) {
        Long userId = getUsuarioId(username);
        carritosActivos.remove(userId);
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DEL CARRITO (PERSISTENTE - INVITADO)
    // ----------------------------------------------------------------------------------

    /**
     * 1. Agrega o actualiza un producto en el carrito temporal del invitado.
     */
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

    /**
     * 2. Obtiene todos los ítems del carrito de invitado.
     */
    public List<GuestCart> getGuestCart(String guestIdentifier) {
        if (guestIdentifier == null || guestIdentifier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de invitado es requerido.");
        }
        return guestCartRepository.findByGuestIdentifier(guestIdentifier);
    }

    /**
     * 3. Elimina un producto específico del carrito de invitado.
     */
    @Transactional
    public void deleteGuestCartItem(String guestIdentifier, Long productId) {
        guestCartRepository.deleteByGuestIdentifierAndProductoId(guestIdentifier, productId);
    }

    /**
     * 4. Vacía completamente el carrito del invitado.
     */
    @Transactional
    public void clearGuestCart(String guestIdentifier) {
        guestCartRepository.deleteByGuestIdentifier(guestIdentifier);
    }


    // ----------------------------------------------------------------------------------
    // LÓGICA DE CHECKOUT (PERSISTENCIA)
    // ----------------------------------------------------------------------------------

    /**
     * Finaliza la compra para un USUARIO REGISTRADO (antiguo método crearPedido, renombrado).
     */
    @Transactional
    public Pedido crearPedidoUsuarioRegistrado(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        Long userId = usuario.getId();
        Map<Long, Integer> carritoIds = carritosActivos.get(userId);

        if (carritoIds == null || carritoIds.isEmpty()) {
            throw new IllegalStateException("El carrito del usuario está vacío.");
        }

        // 1. Crear la cabecera del Pedido
        Pedido nuevoPedido = new Pedido(usuario);
        nuevoPedido.setFechaCreacion(LocalDateTime.now());

        // Llenar campos de receptor con datos del usuario registrado (asumiendo que existen)
        nuevoPedido.setEmailReceptor(usuario.getUsername());
        // Si tienes nombre y otros datos en Usuario, úsalos aquí
        // nuevoPedido.setNombreReceptor(usuario.getNombre());

        List<DetallePedido> detalles = new ArrayList<>();
        double totalPedido = 0.0;

        // 2. Procesar los ítems del carrito y crear los DetallesPedido
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

        // 3. Establecer el total y los detalles en el Pedido
        nuevoPedido.setTotal(totalPedido);
        nuevoPedido.setDetalles(detalles);

        // 4. Guardar en la base de datos
        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        // 5. Limpiar el carrito temporal después del checkout exitoso
        limpiarCarrito(username);

        return pedidoGuardado;
    }

    /**
     * Finaliza la compra para un INVITADO.
     */
    @Transactional
    public Pedido crearPedidoInvitado(CheckoutRequest request) {

        String guestIdentifier = request.getGuestIdentifier();

        // 1. Obtener ítems del carrito de invitado
        List<GuestCart> cartItems = guestCartRepository.findByGuestIdentifier(guestIdentifier);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito de invitado está vacío.");
        }

        // 2. Crear la cabecera del Pedido
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuario(null); // No hay usuario registrado asociado
        nuevoPedido.setFechaCreacion(LocalDateTime.now());

        // Mapear datos del DTO a la cabecera del pedido
        nuevoPedido.setEmailReceptor(request.getEmail());
        nuevoPedido.setNombreReceptor(request.getNombreReceptor());
        nuevoPedido.setDireccionEnvio(request.getDireccionEnvio());
        nuevoPedido.setTelefono(request.getTelefono());

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        double total = 0.0;

        // 3. Crear los DetallePedido
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

        // 4. Actualizar el total y guardar de nuevo
        pedidoGuardado.setTotal(total);
        pedidoRepository.save(pedidoGuardado);

        // 5. Limpiar el Carrito Temporal
        clearGuestCart(guestIdentifier);

        return pedidoGuardado;
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DE FUSIÓN DE CARROS (CART MERGE)
    // ----------------------------------------------------------------------------------

    /**
     * Fusiona un carrito de invitado (GuestCart persistente) al carrito de usuario
     * registrado (en memoria) después de un login/registro.
     */
    @Transactional
    public void fusionarCarrito(String username, String guestIdentifier) {
        if (guestIdentifier == null || guestIdentifier.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        Long userId = usuario.getId();

        // 1. Obtener ítems del carrito de invitado
        List<GuestCart> guestItems = guestCartRepository.findByGuestIdentifier(guestIdentifier);

        if (guestItems.isEmpty()) {
            guestCartRepository.deleteByGuestIdentifier(guestIdentifier);
            return;
        }

        // 2. Obtener el carrito en memoria del usuario (o inicializarlo)
        Map<Long, Integer> userCart = carritosActivos.computeIfAbsent(userId, k -> new HashMap<>());

        // 3. Fusionar ítems
        for (GuestCart guestItem : guestItems) {
            Long productId = guestItem.getProducto().getId();
            Integer cantidadInvitado = guestItem.getCantidad();

            // Sumar las cantidades si el producto ya existía
            userCart.merge(productId, cantidadInvitado, Integer::sum);
        }

        // 4. Limpiar el carrito de invitado (tabla persistente)
        guestCartRepository.deleteByGuestIdentifier(guestIdentifier);
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DE CONSULTA DE PEDIDOS (PERSISTENCIA)
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene el historial de pedidos de un usuario.
     */
    public List<Pedido> obtenerHistorialPedidos(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        return pedidoRepository.findByUsuario(usuario);
    }

    /**
     * Obtiene un pedido específico por ID.
     */
    public Pedido obtenerPedidoPorId(Long pedidoId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        return pedidoRepository.findById(pedidoId)
                // Usamos el campo emailReceptor/nombreReceptor si el usuario es null (invitado),
                // o verificamos que el ID del usuario coincida si no es null.
                .filter(p -> p.getUsuario() == null || p.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado o no pertenece al usuario."));
    }
}