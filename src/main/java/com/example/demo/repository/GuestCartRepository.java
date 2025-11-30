package com.example.demo.repository;

import com.example.demo.model.GuestCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestCartRepository extends JpaRepository<GuestCart, Long> {

    // 1. Obtener todos los ítems para un guestIdentifier específico
    List<GuestCart> findByGuestIdentifier(String guestIdentifier);

    // 2. Obtener un ítem específico de un producto y guest (útil para actualizar cantidad)
    Optional<GuestCart> findByGuestIdentifierAndProductoId(String guestIdentifier, Long productoId);

    // 3. Eliminar todos los ítems de un carrito de invitado (para el checkout o clear)
    @Transactional
    void deleteByGuestIdentifier(String guestIdentifier);

    // 4. Eliminar un producto específico del carrito de invitado
    @Transactional
    void deleteByGuestIdentifierAndProductoId(String guestIdentifier, Long productoId);
}