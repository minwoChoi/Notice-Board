package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.User;
import com.example.demo.model.Comment;
import com.example.demo.model.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long>{
    boolean existsByCommentAndUser(Comment comment, User user);
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
} 
