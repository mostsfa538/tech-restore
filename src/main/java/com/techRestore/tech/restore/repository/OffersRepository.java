package com.techRestore.tech.restore.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.Offer;

@Repository
public interface OffersRepository extends JpaRepository<Offer, UUID> {

    Page<Offer> findByShopId(UUID shopId, Pageable pageable);
}
