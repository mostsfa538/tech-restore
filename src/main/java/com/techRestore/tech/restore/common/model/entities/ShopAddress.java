package com.techRestore.tech.restore.common.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shop_address")
@Data
public class ShopAddress {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "street", nullable = false, columnDefinition = "TEXT")
    private String street;

    @Column(name = "building", nullable = false, length = 50)
    private String building;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}