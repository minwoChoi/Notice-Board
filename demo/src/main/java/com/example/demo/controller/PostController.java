package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType; 

import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostDetailResponse;
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

    // 상세 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> detail(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : null;
        Post post = postService.findPostById(id, username);

        List<CommentResponse> commentResponses = post.getComments().stream()
                .map(CommentResponse::new)
                .toList();

        PostDetailResponse responseDto = new PostDetailResponse();
        responseDto.setPostId(post.getPostId());
        responseDto.setCategoryName(post.getCategory().getCategoryName());
        responseDto.setTitle(post.getTitle());
        responseDto.setContent(post.getContent());
        // responseDto.setPhoto(post.getPhoto()); 

        // 👇 photoUrl 필드에 이미지 API 주소를 만들어서 설정합니다.
        if (post.getPhoto() != null && post.getPhoto().length > 0) {
            responseDto.setPhotoUrl("/posts/" + post.getPostId() + "/photo");
        }

        responseDto.setNickname(post.getUser().getNickname());
        responseDto.setCreatedDate(post.getCreatedDate());
        responseDto.setLikeCount(post.getLikeCount());
        responseDto.setViewCount(post.getViewCount());
        responseDto.setComments(commentResponses);

        return ResponseEntity.ok(responseDto);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/")
    public ResponseEntity<List<PostListResponse>> getAllPosts() {
        // 서비스가 직접 DTO 리스트를 반환하므로, 받아서 그대로 응답하면 끝입니다.
        List<PostListResponse> responseList = postService.findAllPosts();
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
        //response.setPhoto(savedPost.getPhoto());
        if (savedPost.getPhoto() != null && savedPost.getPhoto().length > 0) {
            response.setPhotoUrl("/posts/" + savedPost.getPostId() + "/photo");
        }

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

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPostPhoto(@PathVariable Long id) {
        byte[] photoBytes = postService.getPhotoById(id); // (서비스에 이 메소드 추가 필요)

        if (photoBytes == null) {
            return ResponseEntity.notFound().build();
        }

        // 브라우저가 이 응답을 이미지로 해석하도록 Content-Type을 설정
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 또는 IMAGE_PNG 등
                .body(photoBytes);
    }
}
