package com.example.demo.repository;

import com.example.demo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

// Hereda las operaciones CRUD básicas, pero para la entidad Cake
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}