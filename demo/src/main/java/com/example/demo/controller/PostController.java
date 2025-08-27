package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostEditResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.model.User;
import com.example.demo.model.Post;
import com.example.demo.model.Category;
import com.example.demo.service.PostService;

import lombok.AllArgsConstructor;

import java.util.List;
@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;
    
    //게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostListResponse> detail (@PathVariable Long id,  Authentication authentication) {
        String username = authentication.getName();

        Post post = postService.findPostById(id, username);
        
        PostListResponse dto = new PostListResponse();
        dto.setPostId(post.getPostId());
        dto.setCategoryName(post.getCategory().getCategoryName());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPhoto(post.getPhoto());
        dto.setNickname(post.getUser().getNickname()); // 작성자명
        dto.setCreatedDate(post.getCreatedDate());
        dto.setLikeCount(post.getLikeCount());
        dto.setViewCount(post.getViewCount());
        return ResponseEntity.ok(dto);
    }
    
    //전체 게시글 목록 조회
    @GetMapping("/")
    public ResponseEntity<List<PostListResponse>> getAllPosts() {
        List<Post> posts = postService.findAllPosts();
        
        // 엔티티 리스트를 DTO 리스트로 변환 (매핑)
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
    
    //게시글 작성
    @PostMapping("/")
    public ResponseEntity<PostListResponse> createPost(
            @RequestBody PostCreateRequest postCreateRequest, 
            Authentication authentication) {
    
        String userId = authentication.getName();
        Post savedPost = postService.createPost(postCreateRequest, userId);
    
        // 엔티티 -> DTO 변환
        PostListResponse response = new PostListResponse();
        response.setPostId(savedPost.getPostId());
        response.setCategoryName(savedPost.getCategory().getCategoryName());
        response.setTitle(savedPost.getTitle());
        response.setContent(savedPost.getContent());
        response.setPhoto(savedPost.getPhoto());
        response.setNickname(savedPost.getUser().getNickname()); // 게시글 작성자 닉네임
        response.setCreatedDate(savedPost.getCreatedDate());
        response.setLikeCount(savedPost.getLikeCount());
        response.setViewCount(savedPost.getViewCount());
    
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    
    //게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<PostEditResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostEditRequest postEditRequest,
            Authentication authentication) {

        String username = authentication.getName();

        // 서비스에 id, 수정 DTO, 사용자명을 전달해 게시글 일부 수정 처리
        Post updatedPost = postService.updatePost(id, postEditRequest, username);

        // 수정된 게시글 엔티티를 응답 DTO로 변환
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
        return ResponseEntity.noContent().build(); // 204 No Content, 삭제 성공 시 보통 사용
    }
    
    //게시글 추천
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.likePost(id, username);
        return ResponseEntity.ok().build();
    }
    
    //게시글 추천 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        postService.unlikePost(id, username);
        return ResponseEntity.ok().build();
    }
    
    //게시글 수정 불러오기
}
