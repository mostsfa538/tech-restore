package com.techRestore.tech.restore.Chat.Repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.techRestore.tech.restore.Chat.model.ChatRoom;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

  Optional<ChatRoom> findBySenderIdAndRecipientId(UUID senderId, UUID recipientId);
    
    List<ChatRoom> findBySenderId(UUID senderId);
 
    @Query("{'$or': [{'senderId': ?0}, {'recipientId': ?0}]}")
    List<ChatRoom> findByParticipantId(UUID userId);
    
    Optional<ChatRoom> findByChatId(String chatId);
 
    @Query("{'$or': [{'senderId': ?0, 'recipientId': ?1}, {'senderId': ?1, 'recipientId': ?0}]}")
    Optional<ChatRoom> findByParticipants(UUID user1, UUID user2);
  
    void deleteBySenderIdAndRecipientId(UUID senderId, UUID recipientId);
  
    void deleteByChatId(String chatId);
 
    @Query(value = "{'senderId': ?0}", count = true)
    long countChatRoomsForUser(UUID userId);
}