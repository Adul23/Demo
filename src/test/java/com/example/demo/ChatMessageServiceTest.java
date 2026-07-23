package com.example.demo;

import com.example.demo.models.ChatMessage;
import com.example.demo.models.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private User mockSender;
    private User mockRecipient;

    @BeforeEach
    void setUp() {
        mockSender = new User();
        mockSender.setId(1L);
        mockSender.setUsername("sender");

        mockRecipient = new User();
        mockRecipient.setId(2L);
        mockRecipient.setUsername("recipient");
    }

    @Test
    void saveMessage_ShouldSaveAndReturnMessage() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockRecipient));
        
        ChatMessage message = new ChatMessage(mockSender, mockRecipient, "Hello");
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

        ChatMessage result = chatMessageService.saveMessage(mockSender, 2L, "Hello");

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        assertEquals(mockSender, result.getSender());
        assertEquals(mockRecipient, result.getRecipient());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void getChatHistory_ShouldReturnPageOfMessages() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockRecipient));

        ChatMessage message = new ChatMessage(mockSender, mockRecipient, "Hello");
        Page<ChatMessage> page = new PageImpl<>(List.of(message));
        
        when(chatMessageRepository.findChatHistory(eq(mockSender), eq(mockRecipient), any(Pageable.class)))
                .thenReturn(page);

        Page<ChatMessage> result = chatMessageService.getChatHistory(mockSender, 2L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Hello", result.getContent().get(0).getContent());
        verify(chatMessageRepository, times(1)).findChatHistory(eq(mockSender), eq(mockRecipient), any(Pageable.class));
    }
}
