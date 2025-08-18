package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.post.request.PostCreatRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostEditResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.model.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.PostService;

import java.util.List;
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    
    public PostController(PostRepository postRepository, PostService postService) {
        this.postRepository = postRepository;
        this.postService = postService;
    }

    //게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostListResponse> detail (@PathVariable int id,  Authentication authentication) {
        String username = authentication.getName();

        Post post = postService.findPostById(id, username);
        
        PostListResponse dto = new PostListResponse();
        dto.setPostId(post.getPostId());
        dto.setCategory(post.getCategory());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPhoto(post.getPhoto());
        dto.setUsername(post.getUser().getName());  // 작성자명
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
                    dto.setCategory(post.getCategory());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setPhoto(post.getPhoto());
                    dto.setUsername(post.getUser().getName());
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
    public ResponseEntity<Post> createPost(@RequestBody PostCreatRequest dto, Authentication authentication) {
        String username = authentication.getName();

        // DTO를 Post 엔티티로 변환 (필요하면 별도 매퍼 클래스 만들어도 됨)
        Post post = new Post();
        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setPhoto(dto.getPhoto());

        Post savedPost = postService.createPost(post, username);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    //게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<PostEditResponse> updatePost(
            @PathVariable int id,
            @RequestBody PostEditRequest postEditRequest,
            Authentication authentication) {

        String username = authentication.getName();

        // 서비스에 id, 수정 DTO, 사용자명을 전달해 게시글 일부 수정 처리
        Post updatedPost = postService.updatePost(id, postEditRequest, username);

        // 수정된 게시글 엔티티를 응답 DTO로 변환
        PostEditResponse response = new PostEditResponse();
        response.setPostId(updatedPost.getPostId());
        response.setCategory(updatedPost.getCategory());
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
    public ResponseEntity<Void> deletePost(@PathVariable int id, Authentication authentication) {
        String username = authentication.getName();
        postService.deletePost(id, username);
        return ResponseEntity.noContent().build(); // 204 No Content, 삭제 성공 시 보통 사용
    }
    
    
    
    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id) {
        // 게시글 추천
        return "";
    }

    @DeleteMapping("/{id}/like")
    public String unlikePost(@PathVariable Long id) {
        // 게시글 추천 취소
        return "";
    }



}
