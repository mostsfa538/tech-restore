package com.techRestore.tech.restore.model.entities;

import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "payment_reference", columnDefinition = "TEXT")
    private String paymentReference;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "repair_request_id")
    private UUID repairRequestId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isUserPayment() {
        return user != null && shop == null;
    }

    public boolean isShopPayment() {
        return shop != null && user == null;
    }

    public boolean isOrderPayment(Object order) {
        return order != null;
    }

    public boolean isRepairPayment(Object repairRequest) {
        return repairRequest != null;
    }

    public boolean isSubscriptionPayment(Object subscription) {
        return subscription != null;
    }

    public UUID getPayerId() {
        return user != null ? user.getId() : (shop != null ? shop.getId() : null);
    }

    public Object getPayerEntity() {
        return user != null ? user : shop;
    }
}