package com.techRestore.tech.restore.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name = "chat")
@Data
@Entity
public class Chat {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // PROPER RELATIONSHIPS - Remove UUID fields and use proper JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    private Shop shop;

    // Optional: Link to support ticket if this chat is part of support
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id")
    @JsonIgnore
    private SupportTicket supportTicket;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic helper methods
    public boolean isGeneralChat() {
        return supportTicket == null;
    }

    public boolean isSupportChat() {
        return supportTicket != null;
    }

    // Validation method
    public boolean isValid() {
        // Must have either user or shop (or both for direct communication)
        return user != null || shop != null;
    }
}