package com.techRestore.tech.restore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
  private UUID id;
  private String recipientType;
  private UUID recipientId;
  private UUID senderId;
  private String senderType;
  private String title;
  private String message;
  private String notificationType;
  private UUID relatedEntityId;
  private String relatedEntityType;
  private boolean isRead;
  private LocalDateTime readAt;
  private LocalDateTime createdAt;
}
