package com.techRestore.tech.restore.common.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "category")
@Data
public class Category {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, orphanRemoval = false)
    @JsonIgnore
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        id = UuidCreator.getTimeOrderedEpoch();
        createdAt = LocalDateTime.now();
    }
}