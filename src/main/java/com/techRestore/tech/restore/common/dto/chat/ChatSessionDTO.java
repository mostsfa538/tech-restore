package com.techRestore.tech.restore.common.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ChatSessionDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private UUID shopId;
    private String shopName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private ChatMessageDTO lastMessage;
}
