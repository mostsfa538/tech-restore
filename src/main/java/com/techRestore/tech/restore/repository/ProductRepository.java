package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategoryId(UUID id);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.shopId = :id AND p.category = :category")
    List<Product> findByCategoryWithShopIdAndCategory(@Param("id") UUID id, @Param("category")String category);

    @Query("SELECT p FROM Product p WHERE p.category = :category")
    List<Product> findWithFilters(@Param("category")String category);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max")
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findByShopId(UUID shopId);
}
