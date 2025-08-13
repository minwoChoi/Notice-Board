package com.example.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.repository.PostRepository;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
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
    @PostMapping("path")
    public String createPost(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
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
