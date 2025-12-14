package com.techRestore.tech.restore.common.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.ChatMessage;
import com.techRestore.tech.restore.common.model.entities.ChatMessage.SenderType;
import com.techRestore.tech.restore.common.model.entities.ChatSession;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.id = :sessionId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatSessionIdOrderByCreatedAt(@Param("sessionId") UUID sessionId);

    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);

    Page<ChatMessage> findByChatSessionOrderByCreatedAtDesc(ChatSession chatSession, Pageable pageable);

    List<ChatMessage> findByChatSessionAndIsReadFalse(ChatSession chatSession);

    long countByChatSessionAndIsReadFalse(ChatSession chatSession);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession AND cm.senderType = :senderType ORDER BY cm.createdAt DESC")
    List<ChatMessage> findMessagesBySenderType(
            @Param("chatSession") ChatSession chatSession,
            @Param("senderType") ChatMessage.SenderType senderType);

    List<ChatMessage> findByChatSessionAndSenderTypeAndIsReadFalse(
            ChatSession chatSession,
            SenderType senderType
    );

}
