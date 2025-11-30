package com.example.demo.service;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // NOTA: Se ha ELIMINADO la inyección de GuestCartRepository.

    // --- Lógica CRUD de Producto (Única Responsabilidad) ---

    public List<Producto> getAllProducts() {
        return productoRepository.findAll();
    }

    public Producto getProductById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto saveProduct(Producto producto) {
        return productoRepository.save(producto);
    }

    public void deleteProduct(Long id) {
        productoRepository.deleteById(id);
    }

    // NOTA: Todos los métodos de carrito (addToUserCart, getUserCart,
    // deleteUserCartItem, clearUserCart, addToGuestCart, getGuestCart,
    // deleteGuestCartItem, clearGuestCart) han sido ELIMINADOS de esta clase
    // y deben existir únicamente en PedidoService.
}