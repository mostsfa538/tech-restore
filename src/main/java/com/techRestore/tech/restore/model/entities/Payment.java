package com.techRestore.tech.restore.model.entities;

import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
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

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference", columnDefinition = "TEXT")
    private String paymentReference;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "action_source", columnDefinition = "TEXT")
    private String actionSource;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
