package com.techRestore.tech.restore.shop.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Offer;

@Repository
public interface OffersRepository extends JpaRepository<Offer, UUID> {

    Page<Offer> findByShopId(UUID shopId, Pageable pageable);

    Optional<Offer> findByShopIdAndId(UUID shopId, UUID id);

    @Query("SELECT o FROM Offer o WHERE o.shop.id = :shopId AND " +
            "(LOWER(o.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(o.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Offer> searchByShopIdAndNameOrDescription(UUID shopId, String query, Pageable pageable);
}
