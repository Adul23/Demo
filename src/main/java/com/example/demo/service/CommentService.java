package com.example.demo.service;

import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public Comment addComment(User user, Long postId, String content) {
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found for comment"));

        Comment comment = new Comment(user, post, content);
        Comment savedComment = commentRepository.save(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        return savedComment;
    }

    @Transactional(readOnly = true)
    public Page<Comment> getPostComments(Long postId, int page, int size) {
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found for comments"));

        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);
    }

    @Transactional
    public void deleteCommentOnPost(Long commentId, User user) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Deletion of Comment of post has failed"));
        Post post = comment.getPost();
        boolean isCommentAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isPostAuthor = post.getAuthor().getId().equals(user.getId());
        if (!isPostAuthor && !isCommentAuthor){
            throw new RuntimeException("You are not authorized to delete the comment");
        }

        commentRepository.delete(comment);
        Integer commentsCount = post.getCommentsCount();
        if (commentsCount > 0){
            post.setCommentsCount(commentsCount - 1);
        }
    }
}
