package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Pasteleria", description = "Gestión de Productos de la Pastelería")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // ============================
    //  RUTAS PÚBLICAS
    // ============================

    @GetMapping
    @Operation(summary = "Ver una lista de productos disponibles")
    public List<Producto> getAllProducts() {
        return productoService.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por Id")
    public Producto getProductById(@PathVariable Long id) {
        return productoService.getProductById(id);
    }

    // ============================
    //  RUTAS SOLO PARA ADMIN
    // ============================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Añadir un nuevo producto (ADMIN)")
    public Producto createProduct(@RequestBody Producto producto) {
        return productoService.saveProduct(producto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un producto existente (ADMIN)")
    public Producto updateProduct(@PathVariable Long id, @RequestBody Producto producto) {
        Producto existingProduct = productoService.getProductById(id);
        if (existingProduct != null) {
            existingProduct.setName(producto.getName());
            existingProduct.setPrice(producto.getPrice());
            existingProduct.setDescription(producto.getDescription());
            return productoService.saveProduct(existingProduct);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un producto (ADMIN)")
    public void deleteProduct(@PathVariable Long id) {
        productoService.deleteProduct(id);
    }
}
