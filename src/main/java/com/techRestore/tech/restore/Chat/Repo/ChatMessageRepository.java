package com.techRestore.tech.restore.Chat.Repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.techRestore.tech.restore.Chat.model.ChatMessage;
import com.techRestore.tech.restore.Chat.model.MessageStatus;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    long countBySenderIdAndRecipientIdAndStatus(UUID senderId, UUID recipientId, MessageStatus status);
   
    @Query("{'recipientId': ?0, 'status': ?1}")
    long countUnreadMessagesForRecipient(UUID recipientId, MessageStatus status);
    
    @Query("{'$or': [{'senderId': ?0, 'recipientId': ?1}, {'senderId': ?1, 'recipientId': ?0}], 'recipientId': ?0, 'status': ?2}")
    long countUnreadMessagesInConversation(UUID userId, UUID otherUserId, MessageStatus status);

    @Query(value = "{'chatId': ?0}", sort = "{'timestamp': 1}")
    List<ChatMessage> findByChatId(String chatId);
    
    @Query(value = "{'chatId': ?0}", sort = "{'timestamp': -1}")
    List<ChatMessage> findRecentMessagesByChatId(String chatId, org.springframework.data.domain.Pageable pageable);
    
    @Query("{'recipientId': ?0, 'status': 'RECEIVED'}")
    List<ChatMessage> findUnreadMessagesForUser(UUID userId);
    
    @Query(value = "{'$or': [{'senderId': ?0}, {'recipientId': ?0}]}", fields = "{'chatId': 1}")
    List<String> findChatIdsByUserId(UUID userId);
    
    void deleteByChatId(String chatId);
}