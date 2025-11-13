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

    public List<Producto> getAllProducts() { // Usamos Products para consistencia
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
}