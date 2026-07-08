package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_history", columnList = "sender_id, recipient_id, timestamp DESC")
        }
)
@Getter
@Setter

public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    private LocalDateTime localDateTime = LocalDateTime.now();

    public ChatMessage(User sender, User recipient, String content) {
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
    }


}
