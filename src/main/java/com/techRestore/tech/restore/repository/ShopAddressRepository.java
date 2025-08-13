package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.ShopAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShopAddressRepository extends JpaRepository<ShopAddress, UUID> {

    List<ShopAddress> findShopAddressByShopId(UUID id);
}
