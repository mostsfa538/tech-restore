package com.techRestore.tech.restore.model.entities;

import com.techRestore.tech.restore.model.enums.DeliveryMethod;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.RepairStatus;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "repair_request")
@Data
public class RepairRequest {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "device_category", nullable = false)
    private UUID categoryId;

    private String description;

    @Column(name = "delivery_address_id", nullable = false)
    private UUID deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    private DeliveryMethod deliveryMethod;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private boolean confirmed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RepairStatus status = RepairStatus.SUBMITTED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address", insertable = false, updatable = false)
    private Address deliveryAddressEntity;

    @OneToOne(mappedBy = "repairRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RepairPayment repairPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
