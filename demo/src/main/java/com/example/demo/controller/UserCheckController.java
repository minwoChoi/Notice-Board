package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.repository.UserRepository;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserCheckController {

    private final UserRepository userRepository;
    public UserCheckController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/checkId")
    public ResponseEntity<Map<String, Object>> checkUserId(@RequestParam String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            String msg = "아이디를 입력해주세요.";
            log.warn("[checkUserId] {}", msg); // 백엔드 콘솔 출력
            return ResponseEntity.badRequest().body(Map.of("available", false, "message", msg));
        }

        try {
            boolean available = !userRepository.findByUserId(userId).isPresent();
            String msg = available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";
            if (available) {
                log.info("[checkUserId] {}", msg);
                return ResponseEntity.ok(Map.of("available", true, "message", msg));
            } else {
                log.info("[checkUserId] {}", msg);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("available", false, "message", msg));
            }
        } catch (Exception e) {
            String msg = "서버 오류가 발생했습니다.";
            log.error("[checkUserId] {}", msg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("available", false, "message", msg));
        }
    }

    @GetMapping("/checkNickname")
    public ResponseEntity<Map<String, Object>> checkUserNickname(@RequestParam String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            String msg = "닉네임을 입력해주세요.";
            log.warn("[checkUserNickname] {}", msg);
            return ResponseEntity.badRequest().body(Map.of("available", false, "message", msg));
        }

        try {
            boolean available = !userRepository.findByNickname(nickname).isPresent();
            String msg = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
            if (available) {
                log.info("[checkUserNickname] {}", msg);
                return ResponseEntity.ok(Map.of("available", true, "message", msg));
            } else {
                log.info("[checkUserNickname] {}", msg);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("available", false, "message", msg));
            }
        } catch (Exception e) {
            String msg = "서버 오류가 발생했습니다.";
            log.error("[checkUserNickname] {}", msg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("available", false, "message", msg));
        }
    }
        
}
