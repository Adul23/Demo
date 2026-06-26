package com.example.demo.service;

import com.example.demo.dto.CreatePostDto;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public Post createPost(CreatePostDto dto, User author) {
        Post post = new Post(author, dto.getContent(), dto.getMediaUrl());
        return postRepository.save(post);
    }

    public Page<Post> getUserPosts(User author, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    public Page<Post> getFeed(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findFeedPosts(user, pageable);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("No such post"));
        if (!post.getAuthor().getId().equals(user.getId())){
            throw new RuntimeException("You are not authorized to delete this post");
        }
        postRepository.delete(post);
    }


}
