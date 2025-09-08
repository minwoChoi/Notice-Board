package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostDetailResponse;
import com.example.demo.dto.post.response.PostEditResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.dto.post.response.PostPageResponse;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.service.PostService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    //상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> detail(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : null;

        // 👇 호출하는 메서드 이름만 변경하면 됩니다.
        PostDetailResponse responseDto = postService.getPostDetail(id, username);

        return ResponseEntity.ok(responseDto);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/")
    public ResponseEntity<PostPageResponse> getAllPosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortCode", defaultValue = "0") int sortCode,
            @RequestParam(name = "category", defaultValue = "0") Long category) { // 파라미터 이름을 sortCode로 변경

        // 1. 정렬 코드에 따라 Sort 객체 생성
        Sort sort;
        switch (sortCode) {
            case 1:
                // 좋아요 순
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case 2:
                // 조회수 순 (추천순)
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            default:
                // 0 또는 그 외의 모든 경우 (최신순)
                sort = Sort.by(Sort.Direction.DESC, "createdDate");
                break;
        }

        int zeroBasedPage = Math.max(0, page - 1);

        // 2. Pageable 객체 생성
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);

        // 3. 서비스 호출
        PostPageResponse response = postService.findAllPosts(pageable, category);
        return ResponseEntity.ok(response);
    }

    // 게시글 작성 (수정된 방식)
    @PostMapping(value = "/", consumes = { "multipart/form-data" })
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

        String userId = authentication.getName();

        if (photo != null && !photo.isEmpty()) {
            postCreateRequest.setPhoto(photo.getBytes());
        }

        Post savedPost = postService.createPost(postCreateRequest, userId);

        PostListResponse response = new PostListResponse();
        response.setPostId(savedPost.getPostId());
        response.setCategoryName(savedPost.getCategory().getCategoryName());
        response.setTitle(savedPost.getTitle());
        response.setContent(savedPost.getContent());
        response.setNickname(savedPost.getUser().getNickname());
        response.setCreatedDate(savedPost.getCreatedDate());
        response.setLikeCount(savedPost.getLikeCount());
        response.setViewCount(savedPost.getViewCount());

        // 1. 게시물 사진 URL 설정 (기존 로직)
        if (savedPost.getPhoto() != null && savedPost.getPhoto().length > 0) {
            response.setPhotoUrl("/posts/" + savedPost.getPostId() + "/photo");
        }

        // 2. [추가] 댓글 수는 0으로 설정
        response.setCommentCount(0L); // 새로 만든 게시물이므로 댓글은 0개입니다.

        // 3. [추가] 작성자 프로필 사진 URL 설정
        User author = savedPost.getUser();
        if (author.getProfilePicture() != null && author.getProfilePicture().length > 0) {
            response.setAuthorProfilePictureUrl("/users/" + author.getUserId() + "/photo");
        }

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
        response.setCategoryId(updatedPost.getCategory().getCategoryId());
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

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<PostPageResponse> searchPosts(
            @RequestParam("keyword") String keyword, // 검색 키워드
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortCode", defaultValue = "0") int sortCode) {

        // 정렬 로직은 기존 getAllPosts와 동일합니다.
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

        // 검색 서비스 호출
        PostPageResponse response = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    // 게시글 추천
    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLikePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        boolean isLikedNow = postService.toggleLike(id, username);
        return ResponseEntity.ok(isLikedNow); // 현재 좋아요 상태 전달
    }

    // 게시글 추천 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.unlikePost(id, username);
        return ResponseEntity.ok().build();
    }

    // 사진
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
