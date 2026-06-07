package com.example.demo;

import com.example.demo.controllers.FollowController;
import com.example.demo.models.User;
import com.example.demo.service.FollowService;
import com.example.demo.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowService followService;

    @MockitoBean
    private JwtService jwtService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("follower@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
        );
    }

    @Test
    void followUser_ShouldReturnSuccess_WhenServiceSucceeds() throws Exception {
        doNothing().when(followService).follow(any(), eq(2L));

        mockMvc.perform(post("/users/2/follow")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully followed user"));

        verify(followService, times(1)).follow(any(), eq(2L));
    }

    @Test
    void followUser_ShouldReturnBadRequest_WhenServiceThrows() throws Exception {
        doThrow(new RuntimeException("You cannot follow yourself"))
                .when(followService).follow(any(), eq(1L));

        mockMvc.perform(post("/users/1/follow")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You cannot follow yourself"));
    }

    @Test
    void unfollowUser_ShouldReturnSuccess_WhenServiceSucceeds() throws Exception {
        doNothing().when(followService).unfollow(any(), eq(2L));

        mockMvc.perform(post("/users/2/unfollow")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully unfollowed user"));

        verify(followService, times(1)).unfollow(any(), eq(2L));
    }

    @Test
    void getFollowers_ShouldReturnPageOfUsers() throws Exception {
        User followerUser = new User();
        followerUser.setId(3L);
        followerUser.setEmail("follower3@example.com");

        Page<User> page = new PageImpl<>(List.of(followerUser), PageRequest.of(0, 10), 1);
        when(followService.getFollowers(eq(1L), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/users/1/followers?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].email").value("follower3@example.com"));
    }

    @Test
    void getFollowStats_ShouldReturnCounts() throws Exception {
        when(followService.getFollowersCount(eq(1L))).thenReturn(5L);
        when(followService.getFollowingCount(eq(1L))).thenReturn(10L);

        mockMvc.perform(get("/users/1/follow-stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followersCount").value(5))
                .andExpect(jsonPath("$.followingCount").value(10));
    }
}
