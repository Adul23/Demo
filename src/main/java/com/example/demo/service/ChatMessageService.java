package com.example.demo.service;

import com.example.demo.models.ChatMessage;
import com.example.demo.models.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessage saveMessage(User sender, Long recipientId, String content) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        ChatMessage message = new ChatMessage(sender, recipient, content);
        return chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getChatHistory(User user1, Long user2Id, int page, int size) {
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findChatHistory(user1, user2, PageRequest.of(page, size));
    }
}
