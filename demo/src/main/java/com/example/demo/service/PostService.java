package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 게시글 생성 서비스 메서드
     * @param post 클라이언트로부터 전달받은 게시글 정보 (id, 생성일 등은 무시 권장)
     * @param userId 인증된 사용자의 고유 식별자
     * @return 저장된 Post 엔티티
     * @throws IllegalArgumentException 사용자 없으면 예외 발생
     */
    @Transactional
    public Post createPost(Post post, String userId) {
        // userId로 User 조회, 없으면 예외 발생
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Post 필드 세팅 - 입력 데이터를 그대로 신뢰하지 않고, 필요한 필드만 서버에서 설정
        post.setUser(user);
        post.setCreatedDate(LocalDateTime.now());
        post.setLikeCount(0);
        post.setViewCount(0);

        // 게시글 저장 및 반환
        return postRepository.save(post);
    }
}
