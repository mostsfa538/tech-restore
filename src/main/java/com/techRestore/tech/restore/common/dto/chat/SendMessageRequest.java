package com.techRestore.tech.restore.common.dto.chat;

import java.util.UUID;

import lombok.Data;

@Data
public class SendMessageRequest {
    private UUID sessionId;
    private String content;
}