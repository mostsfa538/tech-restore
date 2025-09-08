package com.techRestore.tech.restore.common.controller.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techRestore.tech.restore.common.dto.chat.ChatMessageDto;
import com.techRestore.tech.restore.common.dto.chat.ChatNotification;
import com.techRestore.tech.restore.common.model.entities.ChatMessage;
import com.techRestore.tech.restore.common.services.chat.ChatMessageService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public void processMessage(@Payload ChatMessageDto chatMessageDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(chatMessageDto.getSenderEmail())) {
            throw new IllegalArgumentException("Unauthorized sender");
        }

        ChatMessage savedMsg = chatMessageService.save(chatMessageDto);
        ChatNotification notification = ChatNotification.builder()
                .id(savedMsg.getId().toString())
                .senderEmail(savedMsg.getSenderEmail())
                .recipientEmail(savedMsg.getRecipientEmail())
                .content(savedMsg.getContent())
                .build();
        messagingTemplate.convertAndSendToUser(
                chatMessageDto.getRecipientEmail(),
                "/queue/messages",
                notification
        );
    }

    @GetMapping("/messages/{senderEmail}/{recipientEmail}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessage>> findChatMessages(
            @PathVariable String senderEmail,
            @PathVariable String recipientEmail) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentEmail.equals(senderEmail) && !currentEmail.equals(recipientEmail)) {
            throw new IllegalArgumentException("Unauthorized access to chat messages");
        }
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderEmail, recipientEmail));
    }
}