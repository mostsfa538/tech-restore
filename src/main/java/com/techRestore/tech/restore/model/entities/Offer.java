package com.techRestore.tech.restore.model.entities;

import com.techRestore.tech.restore.model.enums.OfferType;
import com.techRestore.tech.restore.model.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "offers")
public class Offer {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type")
    private OfferType offerType; // PRODUCT, REPAIR, GENERAL

    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
