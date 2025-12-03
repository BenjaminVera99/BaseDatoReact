package com.example.demo.repository;

import com.example.demo.model.GuestCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestCartRepository extends JpaRepository<GuestCart, Long> {

    List<GuestCart> findByGuestIdentifier(String guestIdentifier);

    Optional<GuestCart> findByGuestIdentifierAndProductoId(String guestIdentifier, Long productoId);

    @Transactional
    void deleteByGuestIdentifier(String guestIdentifier);

    @Transactional
    void deleteByGuestIdentifierAndProductoId(String guestIdentifier, Long productoId);
}