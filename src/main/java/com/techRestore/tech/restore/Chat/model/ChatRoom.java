package com.techRestore.tech.restore.Chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.techRestore.tech.restore.common.model.enums.SenderRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ChatRoom {
    @Id
    private String id;
    private String chatId;
    private UUID senderId;
    private UUID recipientId;
    private SenderRole senderRole;
    private SenderRole recipientRole;
}