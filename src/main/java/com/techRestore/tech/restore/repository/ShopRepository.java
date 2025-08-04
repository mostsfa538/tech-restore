package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.Shop;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends CrudRepository<Shop, UUID> {
    Optional<Shop> findByEmail(String email);
    boolean existsByEmail(String email);
}
