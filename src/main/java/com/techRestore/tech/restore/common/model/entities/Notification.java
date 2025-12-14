package com.techRestore.tech.restore.common.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing notifications for users, shops, delivery personnel, and
 * assigners
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "recipient_type", nullable = false)
  private String recipientType; // "USER", "SHOP", "DELIVERY", "ASSIGNER"

  @Column(name = "recipient_id", nullable = false)
  private UUID recipientId; // ID of the user, shop, delivery, or assigner

  @Column(name = "sender_id")
  private UUID senderId; // Optional: ID of who sent the notification

  @Column(name = "sender_type")
  private String senderType; // Optional: "USER", "SHOP", "DELIVERY", "ASSIGNER", "SYSTEM"

  @Column(nullable = false, columnDefinition = "TEXT")
  private String title;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Column(name = "notification_type", nullable = false)
  private String notificationType; // "CHAT", "ORDER_UPDATE", "REPAIR_UPDATE", "ASSIGNMENT", "DELIVERY_UPDATE",
                                   // "SYSTEM"

  @Column(name = "related_entity_id")
  private UUID relatedEntityId; // ID of the related entity (e.g., OrderId, RepairRequestId)

  @Column(name = "related_entity_type")
  private String relatedEntityType; // "ORDER", "REPAIR_REQUEST", "ASSIGNMENT", etc.

  @Column(name = "is_read")
  private boolean isRead = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (updatedAt == null) {
      updatedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
