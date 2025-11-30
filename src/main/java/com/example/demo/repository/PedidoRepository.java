package com.example.demo.repository;

import com.example.demo.model.Pedido;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Método personalizado para obtener todos los pedidos de un usuario específico.
    // Esto es crucial para la aplicación web y móvil.
    List<Pedido> findByUsuario(Usuario usuario);
}