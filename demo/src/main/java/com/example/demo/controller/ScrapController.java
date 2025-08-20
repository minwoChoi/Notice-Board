package com.example.demo.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ScrapService;
import lombok.AllArgsConstructor;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/posts")
public class ScrapController {

    private final ScrapService scrapService;

    // 게시글 스크랩 추가 (POST /posts/{postId}/scrap)
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<String> addScrap(@PathVariable Long postId, Authentication authentication) {
        String userId = authentication.getName(); // 로그인한 사용자 정보 얻기
        scrapService.addScrap(userId, postId);
        return ResponseEntity.ok("스크랩 추가 완료");
    }

    // 게시글 스크랩 취소 (DELETE /posts/{postId}/scrap)
    @DeleteMapping("/{postId}/scrap")
    public ResponseEntity<String> removeScrap(@PathVariable Long postId, Authentication authentication) {
        String userId = authentication.getName();
        scrapService.removeScrap(userId, postId);
        return ResponseEntity.ok("스크랩 취소 완료");
    }
}


