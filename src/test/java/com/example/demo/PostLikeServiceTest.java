package com.example.demo;

import com.example.demo.models.Post;
import com.example.demo.models.PostLike;
import com.example.demo.models.User;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private LikeService likeService;

    private User mockUser;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("liker@example.com");

        mockPost = new Post(new User(), "Test Post", null);
        mockPost.setId(100L);
        mockPost.setLikesCount(5);
    }

    @Test
    void likePost_ShouldSaveLikeAndIncrementCount_WhenNotAlreadyLiked() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(postLikeRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(false);

        likeService.likePost(mockUser, 100L);

        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(postRepository, times(1)).save(mockPost);
        assertEquals(6, mockPost.getLikesCount());
    }

    @Test
    void likePost_ShouldThrowException_WhenAlreadyLiked() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(postLikeRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> likeService.likePost(mockUser, 100L));
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void unlikePost_ShouldDeleteLikeAndDecrementCount_WhenAlreadyLiked() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(postLikeRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(true);

        likeService.unlikePost(mockUser, 100L);

        verify(postLikeRepository, times(1)).deleteByUserAndPost(mockUser, mockPost);
        verify(postRepository, times(1)).save(mockPost);
        assertEquals(4, mockPost.getLikesCount());
    }

    @Test
    void unlikePost_ShouldThrowException_WhenNotLiked() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(postLikeRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> likeService.unlikePost(mockUser, 100L));
        verify(postLikeRepository, never()).deleteByUserAndPost(any(User.class), any(Post.class));
        verify(postRepository, never()).save(any(Post.class));
    }
}
