package com.techRestore.tech.restore.common.model.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(
        name = "assignment_log",
        indexes = {
                @Index(name = "idx_assignment_order_id", columnList = "order_id"),
                @Index(name = "idx_assignment_repair_id", columnList = "repair_request_id"),
                @Index(name = "idx_assignment_type", columnList = "assignment_type"),
                @Index(name = "idx_assignment_created_at", columnList = "created_at")
        }
)
public class AssignmentLog {

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id", nullable = true)
    private Assigner assigner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = true)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = true)
    private Delivery delivery;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "repair_request_id")
    private UUID repairRequestId;

    @Column(name = "assignment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentType assignmentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum AssignmentType {
        ORDER,
        REPAIR
    }

    @PrePersist
    protected void onCreate() {
        id = UuidCreator.getTimeOrderedEpoch();
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
