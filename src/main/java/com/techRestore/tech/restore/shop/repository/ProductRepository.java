package com.techRestore.tech.restore.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Product;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

      @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.shop.verified = true")
      Page<Product> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);

      @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.stock <= :stockThreshold")
      Page<Product> findByStockLessThanEqual(UUID shopId, int stockThreshold, Pageable pageable);

      @Modifying
      @Query("UPDATE Product p SET p.shopId = :unknownShopId WHERE p.shopId = :shopId")
      void updateShopToUnknown(@Param("shopId") UUID shopId, @Param("unknownShopId") UUID unknownShopId);
}
