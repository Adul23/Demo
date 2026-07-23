package com.example.demo;

import com.example.demo.controllers.ChatController;
import com.example.demo.models.ChatMessage;
import com.example.demo.models.User;
import com.example.demo.service.ChatMessageService;
import com.example.demo.service.JwtService;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatMessageService chatMessageService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
        );
    }

    @Test
    void getChatHistory_ShouldReturnPageOfMessages() throws Exception {
        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("recipient");

        ChatMessage message = new ChatMessage(mockUser, recipient, "Hello controller");
        message.setId(100L);

        Page<ChatMessage> page = new PageImpl<>(List.of(message), PageRequest.of(0, 10), 1);
        when(chatMessageService.getChatHistory(any(User.class), eq(2L), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/messages/2?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].content").value("Hello controller"));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        User userA = new User();
        userA.setId(10L);
        userA.setUsername("userA");

        User userB = new User();
        userB.setId(11L);
        userB.setUsername("userB");

        when(userRepository.findAll()).thenReturn(List.of(mockUser, userA, userB));

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("userA"))
                .andExpect(jsonPath("$[1].username").value("userB"));
    }
}
