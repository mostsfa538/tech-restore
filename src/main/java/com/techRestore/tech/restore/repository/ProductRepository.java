package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("FROM Product p LEFT JOIN p.category c WHERE p.shopId = :shopId AND p.category.id = :categoryId")
    Page<Product> findProductByCategoryId(@Param("shopId") UUID shopId, @Param("categoryId") UUID categoryId,
            Pageable pageable);

    @Query("""
            FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("FROM Product p WHERE p.price BETWEEN :min AND :max")
    Page<Product> findByPriceBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId")
    Page<Product> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);
}
