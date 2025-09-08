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

    @PostMapping("/{postId}/scrap")
    public ResponseEntity<Boolean> toggleScrap(@PathVariable Long postId, Authentication authentication) {
        String userId = authentication.getName();
        boolean isScrapped = scrapService.toggleScrap(userId, postId);
        return ResponseEntity.ok(isScrapped);  // 현재 스크랩 상태 반환
    }

    
}
