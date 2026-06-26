package com.example.demo;

import com.example.demo.models.Post;
import com.example.demo.models.SavedPost;
import com.example.demo.models.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.SavedPostRepository;
import com.example.demo.service.SavedPostService;
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
class SavedPostServiceTest {

    @Mock
    private SavedPostRepository savedPostRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private SavedPostService savedPostService;

    private User mockUser;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("saver@example.com");

        mockPost = new Post(new User(), "Bookmarked post", null);
        mockPost.setId(100L);
    }

    @Test
    void savePost_ShouldSaveBookmark_WhenNotAlreadySaved() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(savedPostRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(false);

        savedPostService.savePost(mockUser, 100L);

        verify(savedPostRepository, times(1)).save(any(SavedPost.class));
    }

    @Test
    void savePost_ShouldThrowException_WhenAlreadySaved() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(savedPostRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> savedPostService.savePost(mockUser, 100L));
        verify(savedPostRepository, never()).save(any(SavedPost.class));
    }

    @Test
    void unSavePost_ShouldDeleteBookmark_WhenAlreadySaved() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(savedPostRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(true);

        savedPostService.unSavePost(mockUser, 100L);

        verify(savedPostRepository, times(1)).deleteByUserAndPost(mockUser, mockPost);
    }

    @Test
    void unSavePost_ShouldThrowException_WhenNotSaved() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(mockPost));
        when(savedPostRepository.existsByUserAndPost(mockUser, mockPost)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> savedPostService.unSavePost(mockUser, 100L));
        verify(savedPostRepository, never()).deleteByUserAndPost(any(User.class), any(Post.class));
    }

    @Test
    void getSavedPosts_ShouldReturnPageOfPosts() {
        SavedPost savedPost = new SavedPost(mockUser, mockPost);
        Page<SavedPost> savedPage = new PageImpl<>(List.of(savedPost), PageRequest.of(0, 10), 1);

        when(savedPostRepository.findByUserOrderBySavedAtDesc(eq(mockUser), any(Pageable.class))).thenReturn(savedPage);

        Page<Post> result = savedPostService.getSavedPosts(mockUser, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Bookmarked post", result.getContent().get(0).getContent());
        verify(savedPostRepository, times(1)).findByUserOrderBySavedAtDesc(eq(mockUser), any(Pageable.class));
    }
}
