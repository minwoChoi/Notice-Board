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

       @Query("SELECT p FROM Post p " +
                     "JOIN FETCH p.user " +
                     "JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.comments c " +
                     "LEFT JOIN FETCH c.user " +
                     "WHERE p.postId = :id")
       Optional<Post> findByIdWithDetails(@Param("id") Long id);

       // 이 쿼리는 이미 GROUP BY 절이 올바르게 작성되어 있습니다. (수정 없음)
       @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
                     "p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, " +
                     "p.user.nickname, p.createdDate, p.viewCount, p.likeCount, COUNT(c), " +
                     "p.isBlocked, " +
                     "CASE WHEN p.photo IS NOT NULL THEN CONCAT('/posts/', p.postId, '/photo') ELSE NULL END, " +
                     "CASE WHEN p.user.profilePicture IS NOT NULL THEN CONCAT('/users/', p.user.userId, '/photo') ELSE NULL END"
                     +
                     ") " +
                     "FROM Post p " +
                     "LEFT JOIN p.comments c ON c.post = p " +
                     "WHERE p.user = :user " +
                     "GROUP BY p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, p.user.nickname, p.createdDate, p.viewCount, p.likeCount, p.isBlocked, p.photo, p.user.profilePicture "
                     +
                     "ORDER BY p.createdDate DESC")
       List<PostListResponse> findPostsByUserWithCommentCount(@Param("user") User user);

       // [수정] findAllWithDetails: 잘못된 countQuery 삭제
       @Query(value = "SELECT new com.example.demo.dto.post.response.PostListResponse(" +
                     "p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, " +
                     "p.user.nickname, p.createdDate, p.viewCount, p.likeCount, COUNT(c), " +
                     "p.isBlocked, " +
                     "CASE WHEN p.photo IS NOT NULL THEN CONCAT('/posts/', p.postId, '/photo') ELSE NULL END, " +
                     "CASE WHEN p.user.profilePicture IS NOT NULL THEN CONCAT('/users/', p.user.userId, '/photo') ELSE NULL END"
                     +
                     ") " +
                     "FROM Post p LEFT JOIN p.comments c ON c.post = p " +
                     "GROUP BY p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, p.user.nickname, p.createdDate, p.viewCount, p.likeCount, p.isBlocked, p.photo, p.user.profilePicture"
       // 👈 countQuery = "SELECT count(p) FROM Post p" 부분을 완전히 삭제했습니다.
       )
       Page<PostListResponse> findAllWithDetails(Pageable pageable);

       // [수정] findByCategoryIdWithDetails: GROUP BY 절 확장
       @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
                     "p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, " +
                     "p.user.nickname, p.createdDate, p.viewCount, p.likeCount, COUNT(c), " +
                     "p.isBlocked, " +
                     "CASE WHEN p.photo IS NOT NULL THEN CONCAT('/posts/', p.postId, '/photo') ELSE NULL END, " +
                     "CASE WHEN p.user.profilePicture IS NOT NULL THEN CONCAT('/users/', p.user.userId, '/photo') ELSE NULL END"
                     +
                     ") " +
                     "FROM Post p LEFT JOIN p.comments c ON c.post = p " +
                     "WHERE p.category.categoryId = :categoryId " +
                     "GROUP BY p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, p.user.nickname, p.createdDate, p.viewCount, p.likeCount, p.isBlocked, p.photo, p.user.profilePicture")
       Page<PostListResponse> findByCategoryIdWithDetails(@Param("categoryId") Long categoryId, Pageable pageable);

       // [수정] findByKeywordWithDetails: GROUP BY 절 확장
       @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
                     "p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, " +
                     "p.user.nickname, p.createdDate, p.viewCount, p.likeCount, COUNT(c), " +
                     "p.isBlocked, " +
                     "CASE WHEN p.photo IS NOT NULL THEN CONCAT('/posts/', p.postId, '/photo') ELSE NULL END, " +
                     "CASE WHEN p.user.profilePicture IS NOT NULL THEN CONCAT('/users/', p.user.userId, '/photo') ELSE NULL END"
                     +
                     ") " +
                     "FROM Post p LEFT JOIN p.comments c ON c.post = p " +
                     "WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% " +
                     "GROUP BY p.postId, p.user.userId, p.category.categoryId, p.category.categoryName, p.title, p.content, p.user.nickname, p.createdDate, p.viewCount, p.likeCount, p.isBlocked, p.photo, p.user.profilePicture")
       Page<PostListResponse> findByKeywordWithDetails(@Param("keyword") String keyword, Pageable pageable);
}