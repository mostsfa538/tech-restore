package com.techRestore.tech.restore.Chat.model;

import java.util.Date;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.SenderRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSummary {
    private UUID otherUserId;
    private String otherUserName;
    private SenderRole otherUserRole;
    private String lastMessage;
    private Date lastMessageTimestamp;
    private long unreadCount;
    private String chatId;
}
