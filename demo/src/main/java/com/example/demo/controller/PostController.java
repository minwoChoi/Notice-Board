package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostEditResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.model.Post;
import com.example.demo.service.PostService;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostListResponse> detail(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Post post = postService.findPostById(id, username);

        PostListResponse dto = new PostListResponse();
        dto.setPostId(post.getPostId());
        dto.setCategoryName(post.getCategory().getCategoryName());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPhoto(post.getPhoto());
        dto.setNickname(post.getUser().getNickname());
        dto.setCreatedDate(post.getCreatedDate());
        dto.setLikeCount(post.getLikeCount());
        dto.setViewCount(post.getViewCount());
        return ResponseEntity.ok(dto);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/")
    public ResponseEntity<List<PostListResponse>> getAllPosts() {
        List<Post> posts = postService.findAllPosts();

        List<PostListResponse> responseList = posts.stream()
                .map(post -> {
                    PostListResponse dto = new PostListResponse();
                    dto.setPostId(post.getPostId());
                    dto.setCategoryName(post.getCategory().getCategoryName());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setPhoto(post.getPhoto());
                    dto.setNickname(post.getUser().getNickname());
                    dto.setCreatedDate(post.getCreatedDate());
                    dto.setLikeCount(post.getLikeCount());
                    dto.setViewCount(post.getViewCount());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(responseList);
    }

    // 게시글 작성 (수정된 방식)
    @PostMapping(value = "/", consumes = { "multipart/form-data" })
    public ResponseEntity<PostListResponse> createPost(
            // 1. DTO 대신 각 필드를 @RequestParam으로 받습니다.
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("categoryId") Long categoryId,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) throws IOException {

        // 2. 받은 파라미터로 DTO를 직접 생성합니다.
        PostCreateRequest postCreateRequest = new PostCreateRequest();
        postCreateRequest.setTitle(title);
        postCreateRequest.setContent(content);
        postCreateRequest.setCategoryId(categoryId);

        String userId = authentication.getName();
        
        // 3. 사진 데이터를 DTO에 추가합니다.
        if (photo != null && !photo.isEmpty()) {
            postCreateRequest.setPhoto(photo.getBytes());
        }
        
        Post savedPost = postService.createPost(postCreateRequest, userId);

        // 응답 생성 로직은 동일
        PostListResponse response = new PostListResponse();
        response.setPostId(savedPost.getPostId());
        response.setCategoryName(savedPost.getCategory().getCategoryName());
        response.setTitle(savedPost.getTitle());
        response.setContent(savedPost.getContent());
        response.setPhoto(savedPost.getPhoto());
        response.setNickname(savedPost.getUser().getNickname());
        response.setCreatedDate(savedPost.getCreatedDate());
        response.setLikeCount(savedPost.getLikeCount());
        response.setViewCount(savedPost.getViewCount());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정 (수정된 방식)
    @PatchMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<PostEditResponse> updatePost(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) throws IOException {

        PostEditRequest postEditRequest = new PostEditRequest();
        postEditRequest.setTitle(title);
        postEditRequest.setContent(content);
        postEditRequest.setCategoryId(categoryId);

        String username = authentication.getName();
        
        if (photo != null && !photo.isEmpty()) {
            postEditRequest.setPhoto(photo.getBytes());
        }
        
        Post updatedPost = postService.updatePost(id, postEditRequest, username);

        // 응답 생성 로직은 동일
        PostEditResponse response = new PostEditResponse();
        response.setPostId(updatedPost.getPostId());
        response.setCategoryName(updatedPost.getCategory().getCategoryName());
        response.setTitle(updatedPost.getTitle());
        response.setContent(updatedPost.getContent());
        response.setPhoto(updatedPost.getPhoto());
        response.setUsername(updatedPost.getUser().getName());
        response.setCreatedDate(updatedPost.getCreatedDate());
        response.setLikeCount(updatedPost.getLikeCount());
        response.setViewCount(updatedPost.getViewCount());

        return ResponseEntity.ok(response);
    }
    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.deletePost(id, username);
        return ResponseEntity.noContent().build();
    }

    // 게시글 추천
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.likePost(id, username);
        return ResponseEntity.ok().build();
    }

    // 게시글 추천 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.unlikePost(id, username);
        return ResponseEntity.ok().build();
    }
}
