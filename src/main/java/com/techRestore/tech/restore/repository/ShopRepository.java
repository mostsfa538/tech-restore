package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {
    Optional<Shop> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT s FROM Shop s WHERE s.name LIKE %:name%")
    List<Shop> findByName(String name);

}
