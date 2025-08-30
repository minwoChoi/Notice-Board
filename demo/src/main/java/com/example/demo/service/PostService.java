package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.model.Post;
import com.example.demo.model.Category;
import com.example.demo.model.PostLike;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    //내가 작성한 게시물 목록 조회
    @Transactional(readOnly = true)
    public List<PostListResponse> findMyPosts(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return postRepository.findPostsByUserWithCommentCount(user);
    }

    //게시글 작성
    @Transactional
    public Post createPost(PostCreateRequest req, String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                
        Category category = categoryRepository.findByCategoryId(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        Post post = new Post();
        post.setUser(user);
        post.setCategory(category);
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setPhoto(req.getPhoto());
        post.setCreatedDate(LocalDateTime.now());
        post.setLikeCount(0);
        post.setViewCount(0);

        return postRepository.save(post);
    }

    //게시글 업데이트
    @Transactional
    public Post updatePost(Long postId, PostEditRequest request, String username) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("이 게시물의 작성자가 아닙니다.");
        }

        // 오직 필요할 때만 카테고리 교체
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
            post.setCategory(category);
        }
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getPhoto() != null) post.setPhoto(request.getPhoto());

        return postRepository.save(post);
    }

    //게시물 삭제
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 검증(로그인한 사용자가 게시글 주인인지)
        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("You are not the author of this post.");
        }

        postRepository.delete(post);
}

    //전체 게시물 조회
    @Transactional(readOnly = true)
    public List<PostListResponse> findAllPosts() {
        return postRepository.findAllWithCommentCount();
    }
    
    //특정 게시물 조회
    @Transactional
    public Post findPostById(Long postId, String username) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 로그인 여부와 관계없이 조회수는 증가시킵니다.
        post.increaseViewCount();
        return post;
    }

    // 게시글 좋아요
    @Transactional
    public void likePost(Long postId, String userId) {
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

        // [추가] 게시글 작성자에게 좋아요 알림 전송 (본인 글 아닐 때만)
        if (!user.getUserId().equals(post.getUser().getUserId())) {
            String message = user.getNickname() + "님이 회원님의 게시물을 추천했습니다.";
            notificationService.createNotification(post.getUser(), message, post, null /*댓글 x*/);
        }
    }

    //게시물 추천 취소
    @Transactional
    public void unlikePost(Long postId, String userId) {
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
    
    @Transactional(readOnly = true)
    public byte[] getPhotoById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        return post.getPhoto();
    }
}
