package com.techRestore.tech.restore.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.ShopAddress;

import java.util.UUID;

@Repository
public interface ShopAddressRepository extends JpaRepository<ShopAddress, UUID> {

    Page<ShopAddress> findShopAddressByShopId(UUID id, Pageable pageable);
}
