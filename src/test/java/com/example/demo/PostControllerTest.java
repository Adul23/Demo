package com.example.demo;

import com.example.demo.controllers.PostController;
import com.example.demo.dto.CreatePostDto;
import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.service.CommentService;
import com.example.demo.service.JwtService;
import com.example.demo.service.LikeService;
import com.example.demo.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private CommentService commentService;

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
    void createPost_ShouldReturnCreatedPost() throws Exception {
        CreatePostDto dto = new CreatePostDto();
        dto.setContent("Hello World");
        dto.setMediaUrl("http://image.png");

        Post mockPost = new Post(mockUser, "Hello World", "http://image.png");
        mockPost.setId(100L);

        when(postService.createPost(any(CreatePostDto.class), any(User.class))).thenReturn(mockPost);

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.content").value("Hello World"))
                .andExpect(jsonPath("$.mediaUrl").value("http://image.png"));
    }

    @Test
    void getMyPosts_ShouldReturnPageOfPosts() throws Exception {
        Post mockPost = new Post(mockUser, "My Post", null);
        mockPost.setId(101L);

        Page<Post> page = new PageImpl<>(List.of(mockPost), PageRequest.of(0, 10), 1);
        when(postService.getUserPosts(any(User.class), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/posts?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(101))
                .andExpect(jsonPath("$.content[0].content").value("My Post"));
    }

    @Test
    void getFeed_ShouldReturnPageOfPosts() throws Exception {
        Post feedPost = new Post(mockUser, "Feed Post", null);
        feedPost.setId(102L);

        Page<Post> page = new PageImpl<>(List.of(feedPost), PageRequest.of(0, 10), 1);
        when(postService.getFeed(any(User.class), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/posts/feed?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(102))
                .andExpect(jsonPath("$.content[0].content").value("Feed Post"));
    }

    @Test
    void likingPost_ShouldReturnMapOfOk() throws Exception {
        Long postId = 100L;
        // Mock the likeService call
        doNothing().when(likeService).likePost(any(User.class), eq(postId));
        mockMvc.perform(post("/posts/" + postId + "/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully liked the post"));
        // Verify the service was actually called
        verify(likeService, times(1)).likePost(any(User.class), eq(postId));
    }

    @Test
    void unlikingPost_ShouldReturnMapOfOk() throws Exception {
        Long postId = 100L;
        // Mock the likeService call
        doNothing().when(likeService).unlikePost(any(User.class), eq(postId));
        mockMvc.perform(post("/posts/" + postId + "/unlike")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully unliked the post"));
        // Verify the service was actually called
        verify(likeService, times(1)).unlikePost(any(User.class), eq(postId));
    }

    @Test
    void postingComment_ShouldReturnMapOfOk() throws Exception {
        Long postId = 100L;
        String content = "Damn";

        Comment mockComment = new Comment(mockUser, new Post(), content);
        when(commentService.addComment(any(User.class), eq(postId), eq(content))).thenReturn(mockComment);

        mockMvc.perform(post("/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)) // Send the comment text as the request body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully commented the post"));

        // Verify the service was actually called with the correct parameters
        verify(commentService, times(1)).addComment(any(User.class), eq(postId), eq(content));
    }

    @Test
    void getPostComments_ShouldReturnPageOfComments() throws Exception {
        Long postId = 100L;
        Comment comment = new Comment(mockUser, new Post(), "Great post!");
        comment.setId(500L);

        Page<Comment> page = new PageImpl<>(List.of(comment), PageRequest.of(0, 10), 1);
        when(commentService.getPostComments(eq(postId), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/posts/" + postId + "/comments?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(500))
                .andExpect(jsonPath("$.content[0].content").value("Great post!"));

        verify(commentService, times(1)).getPostComments(eq(postId), eq(0), eq(10));
    }

}
