package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Pasteleria", description = "Gestión de Productos de la Pastelería")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // GET /api/products
    @GetMapping
    @Operation(summary = "Ver una lista de productos disponibles")
    public List<Producto> getAllProducts() {
        return productoService.getAllProducto();
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por Id")
    public Producto getProducotById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    // POST /api/products
    @PostMapping
    @Operation(summary = "Añadir un nuevo producto")
    public Producto createProducto(@RequestBody Producto producto) {
        return productoService.saveProducto(producto);
    }

    // PUT /api/products/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto existente")
    public Producto updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        Producto existingProducto = productoService.getProductoById(id);
        if (existingProducto != null) {

            existingProducto.setName(producto.getName());
            existingProducto.setPrice(producto.getPrice());
            existingProducto.setDescription(producto.getDescription());
            return productoService.saveProducto(existingProducto); // Guardar los cambios
        }
        return null;
    }

    // DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    public void deleteProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
    }
}