package com.techRestore.tech.restore.common.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;
import com.techRestore.tech.restore.common.model.enums.SubscriptionType;

@Entity
@Table(name = "subscription")
@Data
public class Subscription {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "months", nullable = false)
    private Integer months;

    @Column(name = "base_amount", precision = 10, scale = 2)
    private BigDecimal baseAmount = BigDecimal.valueOf(1000);

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", insertable = false, updatable = false)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        id = UuidCreator.getTimeOrderedEpoch();
        createdAt = LocalDateTime.now();
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(months);
        }
        if (totalAmount == null) {
            totalAmount = baseAmount.multiply(BigDecimal.valueOf(months));
        }
    }
}