package com.techRestore.tech.restore.shop.repository.spec;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.techRestore.tech.restore.common.model.entities.Product;

public class ProductSpecifications {

    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasShop(UUID shopId) {
        return (root, query, cb) -> cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<Product> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> cb.between(root.get("price"), minPrice, maxPrice);
    }

    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Product> shopVerified() {
        return (root, query, cb) -> cb.isTrue(root.get("shop").get("verified"));
    }

    public static Specification<Product> nameOrDescriptionContains(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
        };
    }

}
