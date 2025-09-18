package com.techRestore.tech.restore.common.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.id = :sessionId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatSessionIdOrderByCreatedAt(@Param("sessionId") UUID sessionId);
}
