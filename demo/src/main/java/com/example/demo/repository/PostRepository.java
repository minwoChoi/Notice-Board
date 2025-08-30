package com.example.demo.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.model.Post;
import com.example.demo.model.User;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // (기존) 상세 조회: 매우 좋은 쿼리입니다. 그대로 둡니다.
    @Query("SELECT p FROM Post p " +
           "JOIN FETCH p.user " +
           "JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.comments c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE p.postId = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);
    
    // (기존) 전체 게시물 DTO 조회: 이 메소드 대신 아래에 새로 추가할 메소드를 사용할 것입니다.
    @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
           "p.postId, p.category.categoryName, p.title, p.content, p.photo, " +
           "p.user.nickname, p.createdDate, p.likeCount, p.viewCount, " +
           "(SELECT COUNT(c) FROM Comment c WHERE c.post = p)) " +
           "FROM Post p ORDER BY p.createdDate DESC")
    List<PostListResponse> findAllWithCommentCount();

    // (기존) 특정 사용자 게시물 DTO 조회: 이 메소드는 그대로 사용합니다.
    @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
           "p.postId, p.category.categoryName, p.title, p.content, p.photo, " +
           "p.user.nickname, p.createdDate, p.likeCount, p.viewCount, " +
           "(SELECT COUNT(c) FROM Comment c WHERE c.post = p)) " +
           "FROM Post p WHERE p.user = :user ORDER BY p.createdDate DESC")
    List<PostListResponse> findPostsByUserWithCommentCount(@Param("user") User user);
    
    // 👇 [추가] 전체 게시물 조회 시 N+1 문제 방지를 위해 엔티티를 Fetch Join으로 조회하는 메소드
    @Query("SELECT p FROM Post p JOIN FETCH p.user JOIN FETCH p.category ORDER BY p.createdDate DESC")
    List<Post> findAllPostsWithDetails();
}