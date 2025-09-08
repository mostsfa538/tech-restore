package com.techRestore.tech.restore.common.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.ChatMessage;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE m.chatId = :chatId ORDER BY m.timestamp ASC")
    List<ChatMessage> findByChatId(@Param("chatId") String chatId);
}
