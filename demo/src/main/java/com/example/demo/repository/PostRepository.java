// com/example/demo/repository/PostRepository.java

package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.model.Post;
import com.example.demo.model.User;

public interface PostRepository extends JpaRepository<Post, Long> {

       // 상세 조회 (수정 없음)
       @Query("SELECT p FROM Post p " +
                     "JOIN FETCH p.user " +
                     "JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.comments c " +
                     "LEFT JOIN FETCH c.user " +
                     "WHERE p.postId = :id")
       Optional<Post> findByIdWithDetails(@Param("id") Long id);

       // ▼▼▼ [수정] DTO 생성자와 파라미터 순서를 일치시킵니다 ▼▼▼
       @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
                     "p.postId, " +
                     "p.user.userId, " +
                     "p.category.categoryId, " +
                     "p.category.categoryName, " +
                     "p.title, " +
                     "p.content, " + // <-- 여기에 p.content 추가
                     "p.user.nickname, " +
                     "p.createdDate, p.viewCount, p.likeCount, COUNT(c), " +
                     "CASE WHEN p.photo IS NOT NULL THEN CONCAT('/posts/', p.postId, '/photo') ELSE NULL END, " +
                     "CASE WHEN p.user.profilePicture IS NOT NULL THEN CONCAT('/users/', p.user.userId, '/photo') ELSE NULL END"
                     +
                     ") " +
                     "FROM Post p " +
                     "LEFT JOIN p.comments c ON c.post = p " +
                     "WHERE p.user = :user " +
                     "GROUP BY p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, p.user.nickname, p.createdDate, p.viewCount, p.likeCount, p.photo, p.user.profilePicture "
                     + // <-- GROUP BY 절에도 p.content 추가
                     "ORDER BY p.createdDate DESC")
       List<PostListResponse> findPostsByUserWithCommentCount(@Param("user") User user);

       @Query("SELECT p FROM Post p WHERE p.category.categoryId = :categoryId")
       Page<Post> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

       // 검색 쿼리 (수정 없음)
       @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
       Page<Post> findByTitleContainingOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
}