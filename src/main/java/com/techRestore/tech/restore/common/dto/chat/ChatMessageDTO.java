package com.techRestore.tech.restore.common.dto.chat;

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
public class ChatMessageDTO {
  private UUID id;
  private UUID userId;
  private String userName;
  private UUID shopId;
  private String shopName;
  private String message;
  private String sentBy;
  private LocalDateTime createdAt;
  private boolean isRead;
  private LocalDateTime readAt;
}
