package com.example.demo.controller;

import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostDetailResponse;
import com.example.demo.dto.post.response.PostEditResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.dto.post.response.PostPageResponse;
import com.example.demo.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> detail(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : null;
        PostDetailResponse responseDto = postService.getPostDetail(id, username);
        return ResponseEntity.ok(responseDto);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/")
    public ResponseEntity<PostPageResponse> getAllPosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortCode", defaultValue = "0") int sortCode,
            @RequestParam(name = "category", defaultValue = "0") Long category) {

        Sort sort;
        switch (sortCode) {
            case 1:
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case 2:
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdDate");
                break;
        }

        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);
        PostPageResponse response = postService.findAllPosts(pageable, category);
        return ResponseEntity.ok(response);
    }

    // 전체 게시글 목록 조회
    @GetMapping("")
    public ResponseEntity<PostPageResponse> getAllPosts1(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortCode", defaultValue = "0") int sortCode,
            @RequestParam(name = "category", defaultValue = "0") Long category) {

        Sort sort;
        switch (sortCode) {
            case 1:
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case 2:
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdDate");
                break;
        }

        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);
        PostPageResponse response = postService.findAllPosts(pageable, category);
        return ResponseEntity.ok(response);
    }
    // 게시글 작성
    @PostMapping(value = {"", "/"}, consumes = {"multipart/form-data"})
    public ResponseEntity<PostListResponse> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("categoryId") Long categoryId,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) throws IOException {

        PostCreateRequest postCreateRequest = new PostCreateRequest();
        postCreateRequest.setTitle(title);
        postCreateRequest.setContent(content);
        postCreateRequest.setCategoryId(categoryId);
        if (photo != null && !photo.isEmpty()) {
            postCreateRequest.setPhoto(photo.getBytes());
        }

        String userId = authentication.getName();
        PostListResponse response = postService.createPost(postCreateRequest, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정
    @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
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
        if (photo != null && !photo.isEmpty()) {
            postEditRequest.setPhoto(photo.getBytes());
        }
        
        String username = authentication.getName();
        PostEditResponse response = postService.updatePost(id, postEditRequest, username);

        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.deletePost(id, username);
        return ResponseEntity.noContent().build();
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<PostPageResponse> searchPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortCode", defaultValue = "0") int sortCode) {

        Sort sort;
        switch (sortCode) {
            case 1:
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case 2:
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdDate");
                break;
        }

        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);
        PostPageResponse response = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    // 게시글 추천 토글
    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLikePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        boolean isLikedNow = postService.toggleLike(id, username);
        return ResponseEntity.ok(isLikedNow);
    }

    // 사진 조회
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPostPhoto(@PathVariable Long id) {
        byte[] photoBytes = postService.getPhotoById(id);
        if (photoBytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoBytes);
    }
}