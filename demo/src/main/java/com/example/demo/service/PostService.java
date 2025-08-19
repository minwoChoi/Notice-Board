package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.model.Post;
import com.example.demo.model.PostLike;
import com.example.demo.model.User;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public Post createPost(Post post, String userId) {
        // userId로 User 조회, 없으면 예외 발생
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                //찾지 못하면 orElseThrow 가 실행되어 예외 및 메서드 중단

        // Post 필드 세팅 - 입력 데이터를 그대로 신뢰하지 않고, 필요한 필드만 서버에서 설정
        // Post 구성요소 {postId, user_id(User user), title, content, photo, likeCount, viewCount, ncreatedDate}
        post.setUser(user);
        post.setCreatedDate(LocalDateTime.now());
        post.setLikeCount(0);
        post.setViewCount(0);

        // 게시글 저장 및 반환
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(int postId, PostEditRequest request, String username) {
        // 1. 게시글 조회 (없으면 예외)
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        // 2. 작성자 검증 (ex: post.getUser().getUserId().equals(username) 등)
        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("이 게시물의 작성자가 아닙니다.");
        }

        // 3. 수정할 필드만 반영 (request에 들어온 값만)
        if (request.getCategory() != null) post.setCategory(request.getCategory());
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getPhoto() != null) post.setPhoto(request.getPhoto());

        // 4. 저장 및 반환
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(int postId, String username) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        // 작성자 검증(로그인한 사용자가 게시글 주인인지)
        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("You are not the author of this post.");
        }

        postRepository.delete(post);
}

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }
    
    public Post findPostById(int postId, String username) {
        Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        return post;

    }

    @Transactional
    public void likePost(int postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
                User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 좋아요 눌렀는지 체크
        boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);
        if (alreadyLiked) {
            throw new IllegalStateException("이미 추천한 게시글입니다.");
        }

        // 새 좋아요 저장
        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);

        // 게시글 likeCount +1
        post.increaseLikeCount();
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(int postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
       User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        PostLike like = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalArgumentException("추천하지 않은 게시글입니다."));

        postLikeRepository.delete(like);

        // 게시글 likeCount -1
        post.decreaseLikeCount();
        postRepository.save(post);
    }
}
