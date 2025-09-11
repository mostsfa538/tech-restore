package com.techRestore.tech.restore.Chat.Controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.techRestore.tech.restore.Chat.Service.ChatMessageService;
import com.techRestore.tech.restore.Chat.model.ChatMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Validated
public class ChatController {
    
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload @Valid ChatMessage chatMessage, Principal principal) {
        chatMessageService.processMessage(chatMessage, principal);
    }

    @GetMapping("/messages/{senderId}/{recipientId}/count")
    public ResponseEntity<Long> countNewMessages(
            @PathVariable UUID senderId,
            @PathVariable UUID recipientId,
            Principal principal) {
        
        long count = chatMessageService.countNewMessages(senderId, recipientId, principal);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<?> findChatMessages(
            @PathVariable UUID senderId,
            @PathVariable UUID recipientId,
            Principal principal) {
        
        return chatMessageService.findChatMessages(senderId, recipientId, principal);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<?> findMessage(@PathVariable String id, Principal principal) {
        return chatMessageService.findMessageById(id, principal);
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(Principal principal) {
        return chatMessageService.getUserConversations(principal);
    }

    @PutMapping("/messages/{senderId}/{recipientId}/mark-read")
    public ResponseEntity<?> markMessagesAsRead(
            @PathVariable UUID senderId,
            @PathVariable UUID recipientId,
            Principal principal) {
        
        return chatMessageService.markConversationAsRead(senderId, recipientId, principal);
    }

    @DeleteMapping("/conversations/{otherUserId}")
    public ResponseEntity<?> deleteConversation(
            @PathVariable UUID otherUserId,
            Principal principal) {
        
        return chatMessageService.deleteConversation(otherUserId, principal);
    }

    @GetMapping("/room/exists/{userId1}/{userId2}")
    public ResponseEntity<?> checkChatRoomExists(
            @PathVariable UUID userId1,
            @PathVariable UUID userId2,
            Principal principal) {
        
        return chatMessageService.checkChatRoomExists(userId1, userId2, principal);
    }
}