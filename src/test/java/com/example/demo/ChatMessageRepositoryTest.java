package com.example.demo;

import com.example.demo.models.ChatMessage;
import com.example.demo.models.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindChatHistory() {
        User user1 = new User();
        user1.setUsername("UserOne");
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setEnabled(true);
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("UserTwo");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setEnabled(true);
        user2 = userRepository.save(user2);

        ChatMessage msg1 = new ChatMessage(user1, user2, "Hello User 2!");
        ChatMessage msg2 = new ChatMessage(user2, user1, "Hi User 1!");

        chatMessageRepository.save(msg1);
        chatMessageRepository.save(msg2);

        Page<ChatMessage> chatHistory = chatMessageRepository.findChatHistory(user1, user2, PageRequest.of(0, 10));

        assertThat(chatHistory.getContent()).hasSize(2);
        assertThat(chatHistory.getContent().get(0).getContent()).isEqualTo("Hi User 1!");
        assertThat(chatHistory.getContent().get(1).getContent()).isEqualTo("Hello User 2!");
    }
}
