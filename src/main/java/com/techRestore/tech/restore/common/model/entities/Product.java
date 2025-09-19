package com.techRestore.tech.restore.common.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.UuidCreator;
import com.techRestore.tech.restore.common.model.enums.ProductCondition;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
@Data
public class Product {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer stock;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ProductCondition condition;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    @JsonIgnore
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        id = UuidCreator.getTimeOrderedEpoch();
        createdAt = LocalDateTime.now();
    }

}