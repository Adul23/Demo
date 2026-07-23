package com.example.demo.repository;

import com.example.demo.models.ChatMessage;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender JOIN FETCH cm.recipient " +
            "WHERE (cm.sender = :user1 AND cm.recipient = :user2) " +
            "   OR (cm.sender = :user2 AND cm.recipient = :user1) " +
            "ORDER BY cm.localDateTime DESC")
    Page<ChatMessage> findChatHistory(@Param("user1")User user1, @Param("user2")User user2, Pageable pageable);
}
