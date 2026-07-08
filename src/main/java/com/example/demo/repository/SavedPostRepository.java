package com.example.demo.repository;

import com.example.demo.models.Post;
import com.example.demo.models.SavedPost;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    boolean existsByUserAndPost(User user, Post post);

    void deleteByUserAndPost(User user, Post post);

    @Query("SELECT sp FROM SavedPost sp JOIN FETCH sp.post p JOIN FETCH p.author WHERE sp.user = :user " +
            "ORDER BY sp.savedAt DESC")
    Page<SavedPost> findByUserOrderBySavedAtDesc(@Param("user") User user, Pageable pageable);

    @Query("SELECT sp FROM SavedPost sp JOIN FETCH sp.post p JOIN FETCH p.author WHERE sp.user = :user " +
            "AND LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY sp.savedAt DESC")
    Page<SavedPost> searchSavedPosts(@Param("user") User user, @Param("query") String query, Pageable pageable);

}
