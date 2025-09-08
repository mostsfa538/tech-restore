package com.techRestore.tech.restore.common.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.ChatRoom;

@Repository
public interface ChatRepository extends JpaRepository<ChatRoom, UUID> {
     @Query("SELECT c FROM ChatRoom c WHERE c.senderEmail = :senderEmail AND c.recipientEmail = :recipientEmail")
     Optional<ChatRoom> findBySenderEmailAndRecipientEmail(@Param("senderEmail") String senderEmail, @Param("recipientEmail") String recipientEmail);
}