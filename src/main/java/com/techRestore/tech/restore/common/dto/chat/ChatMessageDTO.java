package com.techRestore.tech.restore.common.dto.chat;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatMessageDTO {
    private UUID id;
    private UUID sessionId;
    private UUID senderId;
    private String senderType;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
}
