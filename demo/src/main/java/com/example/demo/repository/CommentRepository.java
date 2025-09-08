package com.example.demo.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.comment.response.MyCommentResponse;
import com.example.demo.model.Comment;
import com.example.demo.model.User;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCommentId(Long commentId);
    List<Comment> findByUser_UserId(String userId);
    List<Comment> findByPost_PostId(Long postId);

    @Query("SELECT new com.example.demo.dto.comment.response.MyCommentResponse(" +
           "c.commentId, c.content, c.likeCount, c.createdDate, c.post.postId, c.post.title) " +
           "FROM Comment c WHERE c.user = :user ORDER BY c.createdDate DESC")
    List<MyCommentResponse> findCommentsByUserWithPostInfo(@Param("user") User user);
    
} 
