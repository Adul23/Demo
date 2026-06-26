package com.example.demo.service;

import com.example.demo.models.Post;
import com.example.demo.models.SavedPost;
import com.example.demo.models.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.SavedPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavedPostService {
    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;

    public SavedPostService(SavedPostRepository savedPostRepository, PostRepository postRepository) {
        this.savedPostRepository = savedPostRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void savePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (savedPostRepository.existsByUserAndPost(user, post)) {
            throw new RuntimeException("Post is already saved");
        }

        SavedPost savedPost = new SavedPost(user, post);
        savedPostRepository.save(savedPost);
    }

    @Transactional
    public void unSavePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!savedPostRepository.existsByUserAndPost(user, post)) {
            throw new RuntimeException("Post is not saved yet");
        }

        savedPostRepository.deleteByUserAndPost(user, post);
    }

    @Transactional(readOnly = true)
    public Page<Post> getSavedPosts(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SavedPost> savedPage = savedPostRepository.findByUserOrderBySavedAtDesc(user, pageable);
        return savedPage.map(SavedPost::getPost);
    }
}
