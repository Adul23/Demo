package com.example.demo.controllers;

import com.example.demo.dto.CreatePostDto;
import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.models.PostLike;
import com.example.demo.models.User;
import com.example.demo.service.CommentService;
import com.example.demo.service.LikeService;
import com.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    public PostController(PostService postService, LikeService likeService, CommentService commentService) {
        this.postService = postService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getMyPosts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> posts = postService.getUserPosts(user, page, size);
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(
            @AuthenticationPrincipal User user,
            @RequestBody CreatePostDto dto
    ) {
        Post createdPost = postService.createPost(dto, user);
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<Post>> getFeed(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> posts = postService.getFeed(user, page, size);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        try {
            likeService.likePost(user, id);
            return ResponseEntity.ok(java.util.Map.of("message", "Successfully liked the post"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.
                    of("error", e.getMessage()));
        }

    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<?> unlikePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        try {
            likeService.unlikePost(user, id);
            return ResponseEntity.ok(java.util.Map.of("message", "Successfully unliked the post"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.
                    of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> postComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody String content
    ) {
        try {
            this.commentService.addComment(user, id, content);
            return ResponseEntity.ok(java.util.Map.of("message", "Successfully commented the post"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.
                    of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<Comment>> getPostComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Comment> pageComment = commentService.getPostComments(id, page, size);
        return ResponseEntity.ok(pageComment);
    }

}
