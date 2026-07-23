package com.example.demo.controllers;

import com.example.demo.models.ChatMessage;
import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatMessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    // Handles WebSocket STOMP messages sent to /app/chat
    @MessageMapping("/chat")
    public void processMessage(Principal principal, Map<String, Object> payload) {
        User sender = (User) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long recipientId = Long.valueOf(payload.get("recipientId").toString());
        String content = payload.get("content").toString();

        ChatMessage saved = chatMessageService.saveMessage(sender, recipientId, content);

        messagingTemplate.convertAndSendToUser(
                saved.getRecipient().getUsername(),
                "/queue/messages",
                Map.of(
                    "id", saved.getId(),
                    "senderId", saved.getSender().getId(),
                    "senderName", saved.getSender().getUsername(),
                    "content", saved.getContent(),
                    "time", saved.getLocalDateTime().toString()
                )
        );
    }

    // REST endpoint to load message history
    @GetMapping("/messages/{recipientId}")
    public ResponseEntity<Page<ChatMessage>> getChatHistory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(chatMessageService.getChatHistory(currentUser, recipientId, page, size));
    }

    // REST endpoint to load all database users for the chat sidebar
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
