package com.example.demo.service;

import com.example.demo.models.Follow;
import com.example.demo.models.User;
import com.example.demo.repository.FollowRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void follow(User follower, Long followingId) {
        if (follower.getId().equals(followingId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        User targetUser = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        if (followRepository.existsByFollowerAndFollowing(follower, targetUser)) {
            throw new RuntimeException("You are already following this user");
        }

        Follow follow = new Follow(follower, targetUser);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(User follower, Long followingId) {
        User targetUser = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to unfollow not found"));

        if (!followRepository.existsByFollowerAndFollowing(follower, targetUser)) {
            throw new RuntimeException("You are not following this user");
        }

        followRepository.deleteByFollowerAndFollowing(follower, targetUser);
    }

    @Transactional(readOnly = true)
    public Page<User> getFollowers(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findByFollowing(user, pageable).map(Follow::getFollower);
    }

    @Transactional(readOnly = true)
    public Page<User> getFollowing(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findByFollower(user, pageable).map(Follow::getFollowing);
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.countByFollowing(user);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.countByFollower(user);
    }
}
