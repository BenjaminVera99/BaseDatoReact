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
// 💡 SOLUCIÓN CORS: Permite peticiones desde el puerto de desarrollo de React (Vite/npm run dev)
@CrossOrigin(origins = "http://localhost:5173")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // GET /api/products
    @GetMapping
    @Operation(summary = "Ver una lista de productos disponibles")
    public List<Producto> getAllProducts() {
        return productoService.getAllProducts();
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por Id")
    public Producto getProductById(@PathVariable Long id) {
        return productoService.getProductById(id);
    }

    // POST /api/products
    @PostMapping
    @Operation(summary = "Añadir un nuevo producto")
    public Producto createProduct(@RequestBody Producto producto) {
        return productoService.saveProduct(producto);
    }

    // PUT /api/products/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto existente")
    public Producto updateProduct(@PathVariable Long id, @RequestBody Producto producto) {
        Producto existingProduct = productoService.getProductById(id);
        if (existingProduct != null) {
            existingProduct.setName(producto.getName());
            existingProduct.setPrice(producto.getPrice());
            existingProduct.setDescription(producto.getDescription());
            return productoService.saveProduct(existingProduct);
        }
        return null; // El frontend debería manejar un 404/Null
    }

    // DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    public void deleteProduct(@PathVariable Long id) {
        productoService.deleteProduct(id);
    }
}