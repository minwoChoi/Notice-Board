package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Post;
import com.example.demo.model.PostLike;
import com.example.demo.model.User;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostAndUser(Post post, User user);
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPost_PostIdAndUser_UserId(Long postId, String userId);
}
