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

    @Query("SELECT s FROM Scrap s " +
           "JOIN FETCH s.post p " +         // 스크랩된 게시물(Post) 정보를 함께 조회
           "JOIN FETCH p.user " +           // 해당 게시물의 작성자(User) 정보를 함께 조회
           "WHERE s.user = :user " +
           "ORDER BY s.createdDate DESC")
    List<Scrap> findScrapsWithDetailsByUser(@Param("user") User user);

    boolean existsByPost_PostIdAndUser_UserId(Long postId, String userId);;
}