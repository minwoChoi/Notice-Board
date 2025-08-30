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
    
    // LEFT JOIN FETCH를 사용해 게시글, 작성자, 카테고리, 댓글, 댓글 작성자를 한 번에 조회
    @Query("SELECT p FROM Post p " +
           "JOIN FETCH p.user " +
           "JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.comments c " + // 댓글이 없는 경우에도 게시글이 조회되도록 LEFT JOIN 사용
           "LEFT JOIN FETCH c.user " +      // 각 댓글의 작성자 정보도 함께 가져옴
           "WHERE p.postId = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);


    @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
           "p.postId, p.category.categoryName, p.title, p.content, p.photo, " +
           "p.user.nickname, p.createdDate, p.likeCount, p.viewCount, " +
           "(SELECT COUNT(c) FROM Comment c WHERE c.post = p)) " +
           "FROM Post p ORDER BY p.createdDate DESC")
    List<PostListResponse> findAllWithCommentCount();

    // 👇 특정 사용자가 작성한 게시물 목록과 댓글 개수를 함께 조회하는 쿼리 추가
    @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
           "p.postId, p.category.categoryName, p.title, p.content, p.photo, " +
           "p.user.nickname, p.createdDate, p.likeCount, p.viewCount, " +
           "(SELECT COUNT(c) FROM Comment c WHERE c.post = p)) " +
           "FROM Post p WHERE p.user = :user ORDER BY p.createdDate DESC")
    List<PostListResponse> findPostsByUserWithCommentCount(@Param("user") User user);
}
