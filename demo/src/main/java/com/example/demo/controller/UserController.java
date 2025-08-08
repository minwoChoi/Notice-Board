package com.example.demo.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.demo.dto.user.requset.UserReisterRequest;
import com.example.demo.service.AuthService;
import com.example.demo.controller.*;
@RestController
@RequestMapping("/users")
public class UserController {
    private final AuthService userAuthService;   

    // 생성자에서 세 필드 모두 초기화
    public UserController( AuthService userAuthService)
     {
        this.userAuthService = userAuthService;      
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserReisterRequest request) {
        try {
            userAuthService.register(request);
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    // @GetMapping("/me")
    // public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String token) {
    //     // 토큰에서 userId 추출(JWT나 세션 방식에 따라 다름)
    //     String userId = "testuser1";
    //     //userAuthService.extractUserIdFromToken(token);
    //     // 유저 정보 조회
    //     var userInfo = userAuthService.getUserInfo(userId);
    //     if (userInfo == null) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 유저를 찾을 수 없습니다.");
    //     }
    //     return ResponseEntity.ok(userInfo);
    // }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() { // Authorization 헤더도 생략 가능
        String userId = "testuser1"; // 직접 원하는 아이디
        var userInfo = userAuthService.getUserInfo(userId);
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 유저를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(userInfo);
    }




}
