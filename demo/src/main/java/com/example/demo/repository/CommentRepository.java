package com.example.demo.repository;


import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCommentId(Long commentId);
    List<Comment> findByUser_UserId(String userId);
    List<Comment> findByPost_PostId(Long postId);

    
} 
