package com.techRestore.tech.restore.model.entities;

import com.techRestore.tech.restore.model.enums.DeliveryMethod;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
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

    @Column(name = "device_category")
    private String deviceCategory;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    private DeliveryMethod deliveryMethod;

    @Column(name = "delivery_address_id")
    private UUID deliveryAddressId;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", insertable = false, updatable = false)
    private Address deliveryAddress;

    // One repair request can have one payment
    @OneToOne(mappedBy = "repairRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RepairPayment repairPayment;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
