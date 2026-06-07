package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.service.FollowService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        try {
            followService.follow(user, id);
            return ResponseEntity.ok(Map.of("message", "Successfully followed user"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unfollow")
    public ResponseEntity<?> unfollowUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        try {
            followService.unfollow(user, id);
            return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<Page<User>> getFollowers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> followers = followService.getFollowers(id, page, size);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<Page<User>> getFollowing(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> following = followService.getFollowing(id, page, size);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{id}/follow-stats")
    public ResponseEntity<?> getFollowStats(@PathVariable Long id) {
        try {
            long followersCount = followService.getFollowersCount(id);
            long followingCount = followService.getFollowingCount(id);
            return ResponseEntity.ok(Map.of(
                    "followersCount", followersCount,
                    "followingCount", followingCount
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
