package com.example.demo;

import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.CommentService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private User mockUser;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("author@example.com");

        mockPost = new Post(new User(), "Test Post", null);
        mockPost.setId(100L);
        mockPost.setCommentsCount(3);
    }

    @Test
    void addComment_ShouldSaveCommentAndIncrementCount() {
        String content = "My comment content";
        Comment mockComment = new Comment(mockUser, mockPost, content);

        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        Comment savedComment = commentService.addComment(mockUser, 100L, content);

        assertNotNull(savedComment);
        assertEquals(content, savedComment.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(postRepository, times(1)).save(mockPost);
        assertEquals(4, mockPost.getCommentsCount());
    }

    @Test
    void addComment_ShouldThrowException_WhenPostNotFound() {
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.addComment(mockUser, 100L, "test"));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void getPostComments_ShouldReturnPageOfComments() {
        Comment comment = new Comment(mockUser, mockPost, "Hello");
        Page<Comment> page = new PageImpl<>(List.of(comment), PageRequest.of(0, 10), 1);

        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(commentRepository.findByPostOrderByCreatedAtAsc(eq(mockPost), any(Pageable.class))).thenReturn(page);

        Page<Comment> result = commentService.getPostComments(100L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Hello", result.getContent().get(0).getContent());
        verify(commentRepository, times(1)).findByPostOrderByCreatedAtAsc(eq(mockPost), any(Pageable.class));
    }
}
