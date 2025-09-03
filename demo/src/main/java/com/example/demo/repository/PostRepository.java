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

    // [삭제] 이 복잡한 JPQL 쿼리 대신, 서비스 계층에서 DTO로 변환하는 방식을 사용할 것입니다.
    // Page<PostListResponse> findAllWithCommentCount(Pageable pageable);

    // [수정] 내 게시물 조회 쿼리의 생성자를 DTO와 일치시키고, GROUP BY 추가 및 photo 필드 검사를 수정합니다.
    @Query("SELECT new com.example.demo.dto.post.response.PostListResponse(" +
           "p.postId, p.category.categoryName, p.title, p.user.nickname, " +
           "p.createdDate, p.viewCount, p.likeCount, COUNT(c), " +
           "CASE WHEN p.photo IS NOT NULL THEN CONCAT('/posts/', p.postId, '/photo') ELSE NULL END, " +
           "CASE WHEN p.user.profilePicture IS NOT NULL THEN CONCAT('/users/', p.user.userId, '/photo') ELSE NULL END" +
           ") " +
           "FROM Post p " +
           "LEFT JOIN p.comments c ON c.post = p " +
           "WHERE p.user = :user " +
           "GROUP BY p.postId, p.category.categoryName, p.title, p.user.nickname, p.createdDate, p.viewCount, p.likeCount, p.photo, p.user.profilePicture, p.user.userId " +
           "ORDER BY p.createdDate DESC")
    List<PostListResponse> findPostsByUserWithCommentCount(@Param("user") User user);

    // [삭제] 이 쿼리도 사용하지 않습니다.
    // Page<Post> findAllPostsWithDetails(Pageable pageable);
}