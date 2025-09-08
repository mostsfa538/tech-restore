package com.techRestore.tech.restore.common.services.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.dto.chat.ChatMessageDto;
import com.techRestore.tech.restore.common.model.entities.ChatMessage;
import com.techRestore.tech.restore.common.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final MessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessageDto chatMessageDto) {
        String chatId = chatRoomService.getChatRoomId(chatMessageDto.getSenderEmail(), chatMessageDto.getRecipientEmail(), true)
                .orElseThrow(() -> new RuntimeException("Unable to create chat room"));
        ChatMessage chatMessage = ChatMessage.builder()
                .chatId(chatId)
                .senderEmail(chatMessageDto.getSenderEmail())
                .recipientEmail(chatMessageDto.getRecipientEmail())
                .content(chatMessageDto.getContent())
                .build();
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> findChatMessages(String senderEmail, String recipientEmail) {
        Optional<String> chatId = chatRoomService.getChatRoomId(senderEmail, recipientEmail, false);
        return chatId.map(chatMessageRepository::findByChatId).orElse(new ArrayList<>());
    }
}