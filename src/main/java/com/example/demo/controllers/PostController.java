package com.example.demo.controllers;

import com.example.demo.dto.CreatePostDto;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    public PostController(PostService postService){
        this.postService = postService;
    }
    @GetMapping
    public ResponseEntity<Page<Post>> getMyPosts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Post> posts = postService.getUserPosts(user, page, size);
        return ResponseEntity.ok(posts);
    }
    @PostMapping
    public ResponseEntity<Post> createPost(
            @AuthenticationPrincipal User user,
            @RequestParam String content,
            @RequestParam String mediaUrl
    ) {
        CreatePostDto dto = new CreatePostDto();
        dto.setContent(content);
        dto.setMediaUrl(mediaUrl);
        Post createdPost = postService.createPost(dto, user);
        return ResponseEntity.ok(createdPost);
    }
}
