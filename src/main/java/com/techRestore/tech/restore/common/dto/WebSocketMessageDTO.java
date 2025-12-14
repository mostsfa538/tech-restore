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
public class WebSocketMessageDTO {
  private String type;
  private String action;
  private Object payload;
  private UUID senderId;
  private String senderType;
  private UUID recipientId;
  private String status;
  private String message;
  private LocalDateTime timestamp;
}
