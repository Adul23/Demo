package com.example.demo.service;

import com.example.demo.models.Post;
import com.example.demo.models.PostLike;
import com.example.demo.models.User;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public LikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void likePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new RuntimeException("Post is already liked");
        }

        PostLike postLike = new PostLike(user, post);
        postLikeRepository.save(postLike);

        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!postLikeRepository.existsByUserAndPost(user, post)) {
            throw new RuntimeException("Post is not liked yet");
        }

        postLikeRepository.deleteByUserAndPost(user, post);

        if (post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
            postRepository.save(post);
        }
    }
}
