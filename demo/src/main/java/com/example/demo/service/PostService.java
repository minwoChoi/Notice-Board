package com.example.demo.service;

import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostDetailResponse;
import com.example.demo.dto.post.response.PostEditResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.dto.post.response.PostPageResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;
    private final CommentLikeRepository commentLikeRepository;
    private final ScrapRepository scrapRepository;

    // 내가 작성한 게시물 목록 조회
    @Transactional(readOnly = true)
    public List<PostListResponse> findMyPosts(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<PostListResponse> responseList = postRepository.findPostsByUserWithCommentCount(user);
        String profilePictureUrl = (user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                ? "/users/" + userId + "/photo"
                : null;
        responseList.forEach(dto -> dto.setAuthorProfilePictureUrl(profilePictureUrl));
        return responseList;
    }

    // 게시글 작성
    @Transactional
    public PostListResponse createPost(PostCreateRequest req, String userId) {
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

        Post savedPost = postRepository.save(post);

        // 서비스 계층 내에서 DTO로 변환하여 반환
        String photoUrl = (savedPost.getPhoto() != null && savedPost.getPhoto().length > 0)
                ? "/posts/" + savedPost.getPostId() + "/photo" : null;
        String authorProfilePictureUrl = (user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                ? "/users/" + user.getUserId() + "/photo" : null;

        return new PostListResponse(
                savedPost.getPostId(),
                user.getUserId(),
                category.getCategoryId(),
                category.getCategoryName(),
                savedPost.getTitle(),
                savedPost.getContent(),
                user.getNickname(),
                savedPost.getCreatedDate(),
                savedPost.getViewCount(),
                savedPost.getLikeCount(),
                0L, // 새 글이므로 댓글 수는 0
                savedPost.isBlocked(),
                photoUrl,
                authorProfilePictureUrl
        );
    }

    // 게시글 업데이트
    @Transactional
    public PostEditResponse updatePost(Long postId, PostEditRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("이 게시물의 작성자가 아닙니다.");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
            post.setCategory(category);
        }
        if (request.getTitle() != null)
            post.setTitle(request.getTitle());
        if (request.getContent() != null)
            post.setContent(request.getContent());
        if (request.getPhoto() != null)
            post.setPhoto(request.getPhoto());

        Post updatedPost = postRepository.save(post);

        // 서비스 계층 내에서 DTO로 변환하여 반환
        PostEditResponse response = new PostEditResponse();
        response.setPostId(updatedPost.getPostId());
        response.setCategoryId(updatedPost.getCategory().getCategoryId());
        response.setCategoryName(updatedPost.getCategory().getCategoryName());
        response.setTitle(updatedPost.getTitle());
        response.setContent(updatedPost.getContent());
        response.setPhoto(updatedPost.getPhoto());
        response.setUsername(updatedPost.getUser().getName());
        response.setCreatedDate(updatedPost.getCreatedDate());
        response.setLikeCount(updatedPost.getLikeCount());
        response.setViewCount(updatedPost.getViewCount());

        return response;
    }

    // 게시물 삭제
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("You are not the author of this post.");
        }
        postRepository.delete(post);
    }

    // 게시물 검색 (N+1 해결)
    @Transactional(readOnly = true)
    public PostPageResponse searchPosts(String keyword, Pageable pageable) {
        Page<PostListResponse> postDtoPage = postRepository.findByKeywordWithDetails(keyword, pageable);
        return new PostPageResponse(postDtoPage.getContent(), postDtoPage.getTotalElements());
    }

    // 전체/카테고리별 게시물 조회 (N+1 해결)
    @Transactional(readOnly = true)
    public PostPageResponse findAllPosts(Pageable pageable, Long category) {
        Page<PostListResponse> postDtoPage;
        if (category == null || category == 0L) {
            postDtoPage = postRepository.findAllWithDetails(pageable);
        } else {
            postDtoPage = postRepository.findByCategoryIdWithDetails(category, pageable);
        }
        return new PostPageResponse(postDtoPage.getContent(), postDtoPage.getTotalElements());
    }

    // 특정 게시물 조회
    @Transactional
    public PostDetailResponse getPostDetail(Long postId, String username) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.increaseViewCount();

        boolean isPostLiked = false;
        boolean isPostScrapped = false;
        if (username != null) {
            isPostLiked = postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, username);
            isPostScrapped = scrapRepository.existsByPost_PostIdAndUser_UserId(postId, username);
        }

        List<Comment> comments = post.getComments();
        Set<Long> likedCommentIds = (username != null && !comments.isEmpty())
                ? commentLikeRepository.findLikedCommentIdsByUserAndComments(username, comments)
                : Collections.emptySet();

        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    boolean isCommentAuthor = username != null && username.equals(comment.getUser().getUserId());
                    boolean isCommentLiked = likedCommentIds.contains(comment.getCommentId());
                    return new CommentResponse(comment, isCommentAuthor, isCommentLiked);
                })
                .toList();

        PostDetailResponse responseDto = new PostDetailResponse();
        responseDto.setPostId(post.getPostId());
        responseDto.setCategoryId(post.getCategory().getCategoryId());
        responseDto.setTitle(post.getTitle());
        responseDto.setContent(post.getContent());
        responseDto.setNickname(post.getUser().getNickname());
        responseDto.setCreatedDate(post.getCreatedDate());
        responseDto.setLikeCount(post.getLikeCount());
        responseDto.setViewCount(post.getViewCount());
        responseDto.setComments(commentResponses);
        responseDto.setBlocked(post.isBlocked());

        if (post.getPhoto() != null && post.getPhoto().length > 0) {
            responseDto.setPhotoUrl("/posts/" + post.getPostId() + "/photo");
        }
        User author = post.getUser();
        if (author.getProfilePicture() != null && author.getProfilePicture().length > 0) {
            responseDto.setAuthorProfilePictureUrl("/users/" + author.getUserId() + "/photo");
        }
        boolean isPostAuthor = username != null && username.equals(author.getUserId());
        responseDto.setAuthor(isPostAuthor);
        responseDto.setLiked(isPostLiked);
        responseDto.setScrapped(isPostScrapped);
        return responseDto;
    }

    // 게시물 사진 출력
    @Transactional(readOnly = true)
    public byte[] getPhotoById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return post.getPhoto();
    }

    // 토글 좋아요
    @Transactional
    public boolean toggleLike(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다"));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 없습니다"));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.decreaseLikeCount();
            postRepository.save(post);
            return false; // 좋아요 취소됨
        } else {
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            postLikeRepository.save(newLike);
            post.increaseLikeCount();
            postRepository.save(post);

            if (!user.getUserId().equals(post.getUser().getUserId())) {
                String message = user.getNickname() + "님이 회원님의 게시물을 추천했습니다.";
                notificationService.createNotification(post.getUser(), message, post, null);
            }
            return true; // 좋아요 추가됨
        }
    }
}