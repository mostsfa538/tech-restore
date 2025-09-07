package com.techRestore.tech.restore.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Product;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

      @Query("""
                      SELECT p FROM Product p
                      WHERE p.shop.id = :shopId
                        AND p.category.id = :categoryId
                        AND p.shop.verified = true
                  """)
      Page<Product> findProductByCategoryId(
                  @Param("shopId") UUID shopId,
                  @Param("categoryId") UUID categoryId,
                  Pageable pageable);

      @Query("""
                      FROM Product p
                      WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                         OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        AND p.shop.verified = true
                  """)
      Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

      @Query("FROM Product p WHERE p.price BETWEEN :min AND :max AND p.shop.verified = true")
      Page<Product> findByPriceBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max, Pageable pageable);

      @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.shop.verified = true")
      Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

      @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.shop.verified = true")
      Page<Product> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);

      @Query("SELECT p FROM Product p WHERE p.shop.verified = true and p.deleted = false")
      Page<Product> findAllVerified(Pageable pageable);
}
