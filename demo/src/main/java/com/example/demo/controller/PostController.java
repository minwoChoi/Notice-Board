package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.PostService;


@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    
    public PostController(PostRepository postRepository, PostService postService) {
        this.postRepository = postRepository;
        this.postService = postService;
    }
    //GET
    @GetMapping("/")
    public String listLootUp(@RequestParam String param) {
        return new String();
    }
    
    @GetMapping("/upadte")
    public String potWritePage(@RequestParam String param) {
        return new String();
    }

    @GetMapping("path")
    public String postEditPage(@RequestParam String param) {
        return new String();
    }
    
    //POST
    @PostMapping("/")
    public ResponseEntity<Post> createPost(@RequestBody Post post, Authentication authentication) {
        // Authentication에서 username 추출 (일반적으로 getName() 사용)
        String username = authentication.getName();

        // 서비스로 위임하여 Post 생성 처리
        Post savedPost = postService.createPost(post, username);

        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    
    @PutMapping("/{id}")
    public String updatePost(@PathVariable Long id) {
        // 게시글 수정
        return "";
    }

    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id) {
        // 게시글 삭제
        return "";
    }

    @GetMapping("/{id}")
    public String getPostDetail(@PathVariable Long id) {
        // 게시글 상세 조회
        return "";
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
