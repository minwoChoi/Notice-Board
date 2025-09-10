package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Query 어노테이션 import
import org.springframework.data.repository.query.Param; // Param 어노테이션 import

import com.example.demo.model.Post;
import com.example.demo.model.Scrap;
import com.example.demo.model.User;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    boolean existsByUserAndPost(User user, Post post);

    Optional<Scrap> findByUserAndPost(User user, Post post);

    List<Scrap> findByUser(User user);

    @Query("SELECT DISTINCT s FROM Scrap s " + // 1. DISTINCT 추가
            "JOIN FETCH s.post p " +
            "JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.comments c " + // 2. 댓글 정보를 함께 가져오도록 LEFT JOIN FETCH 추가
            "WHERE s.user = :user " +
            "ORDER BY s.createdDate DESC")
    List<Scrap> findScrapsWithDetailsByUser(@Param("user") User user);

    boolean existsByPost_PostIdAndUser_UserId(Long postId, String userId);;
}