package com.example.demo.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.repository.UserRepository;
import com.example.demo.dto.user.requset.UserLoginRequset;
import com.example.demo.model.User;
// import com.example.demo.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final AuthService userAuthService;   

    // 생성자에서 세 필드 모두 초기화
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder
    //, AuthService userAuthService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        // this.userAuthService = userAuthService;      
    }

    // AuthService.java (또는 UserService.java)
    public String extractUserIdFromToken(String token) {
        // 아직 JWT/토큰 사용 안 하면 예시로 임시 구현
        // return 토큰에서 userId 파싱
        throw new UnsupportedOperationException("토큰 미지원 상태");
    }

    public User getUserInfo(String userId) {
        // userRepository 등에서 userId로 User 객체 조회
        return userRepository.findByUserId(userId).orElse(null);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequset loginRequest) {
        Optional<User> userOptional = userRepository.findByUserId(loginRequest.getUserId());  // camelCase getter
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 없음");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호 틀림");
        }

        return ResponseEntity.ok("로그인 성공");
    }

    

}
