package com.techRestore.tech.restore.common.controller.chat;

import com.techRestore.tech.restore.common.dto.chat.ChatMessageDTO;
import com.techRestore.tech.restore.common.services.Chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatRestController {

  private final ChatService chatService;

  @GetMapping("/{userId}/shop/{shopId}")
  public ResponseEntity<List<ChatMessageDTO>> getChatMessages(
      @PathVariable UUID userId,
      @PathVariable UUID shopId) {
    log.info("Fetching chat messages for user: {} and shop: {}", userId, shopId);
    List<ChatMessageDTO> messages = chatService.getChatMessages(userId, shopId);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/{userId}/shop/{shopId}/paginated")
  public ResponseEntity<Page<ChatMessageDTO>> getChatMessagesPaginated(
      @PathVariable UUID userId,
      @PathVariable UUID shopId,
      Pageable pageable) {
    log.info("Fetching paginated chat messages for user: {} and shop: {}", userId, shopId);
    Page<ChatMessageDTO> messages = chatService.getChatMessagesPaginated(userId, shopId, pageable);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/{userId}/unread-count")
  public ResponseEntity<Map<String, Long>> getUnreadMessageCount(@PathVariable UUID userId) {
    log.info("Getting unread message count for user: {}", userId);
    long unreadCount = chatService.getUnreadMessageCount(userId);
    return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
  }

  @PutMapping("/{userId}/shop/{shopId}/mark-read")
  public ResponseEntity<Map<String, String>> markMessagesAsRead(
      @PathVariable UUID userId,
      @PathVariable UUID shopId) {
    log.info("Marking messages as read for user: {} and shop: {}", userId, shopId);
    try {
      chatService.markMessagesAsRead(userId, shopId,
          com.techRestore.tech.restore.common.model.entities.ChatMessage.SenderType.USER);
      return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{userId}/shop/{shopId}/close")
  public ResponseEntity<Map<String, String>> closeChatSession(
      @PathVariable UUID userId,
      @PathVariable UUID shopId) {
    log.info("Closing chat session for user: {} and shop: {}", userId, shopId);
    try {
      chatService.closeChatSession(userId, shopId);
      return ResponseEntity.ok(Map.of("message", "Chat session closed"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
