package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Comment;
import com.example.demo.model.CommentLike;
import com.example.demo.model.User;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    @Query("SELECT cl.comment.commentId FROM CommentLike cl WHERE cl.user.userId = :userId AND cl.comment IN :comments")
    Set<Long> findLikedCommentIdsByUserAndComments(@Param("userId") String userId, @Param("comments") List<Comment> comments);
}