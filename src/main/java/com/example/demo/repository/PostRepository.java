package com.example.demo.repository;

import com.example.demo.models.Post;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author = :user OR p.author IN (SELECT f.following FROM Follow f WHERE f.follower = :user) ORDER BY p.createdAt DESC")
    Page<Post> findFeedPosts(@Param("user") User user, Pageable pageable);
}
