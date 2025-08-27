package com.example.demo.controller;

import com.example.demo.dto.scrap.response.ScrapResponseDto;
import com.example.demo.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class ScrapController {

    private final ScrapService scrapService;

    // 게시글 스크랩 추가 (POST /posts/{postId}/scrap)
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<String> addScrap(@PathVariable Long postId, Authentication authentication) {
        String userId = authentication.getName();
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
