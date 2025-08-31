package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByEmail(String email);
    boolean existsByEmail(String email);
}
