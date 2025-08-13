package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import com.example.demo.dto.user.requset.UserReisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;

import com.example.demo.dto.user.requset.UserDeleteRequest;
import com.example.demo.dto.user.requset.UserEditRequset;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final AuthService userAuthService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 생성자에서 세 필드 모두 초기화
    public UserController(
        AuthService userAuthService,
        UserService userService,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
        ){
        this.userAuthService = userAuthService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //회원가입
    @PostMapping("/")
    public ResponseEntity<String> register(@RequestBody UserReisterRequest request) {
        try {
            userAuthService.register(request);
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        // JWTAuthenticationFilter에서 이미 인증 성공 상태로 SecurityContext에 저장됨
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // 토큰의 subject 값 == userId
        log.info("userId: {}", userId); 
        var userInfo = userAuthService.getUserInfo(userId);
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("해당 유저(" + userId + ")를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping("/")
    public ResponseEntity<String> editProfile(@RequestBody UserEditRequset editRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if ("anonymousUser".equals(userId) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인 후 이용 가능한 기능입니다.");
        }
        try {
            userService.editProfile(userId, editRequest);
            return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe(@RequestBody UserDeleteRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        if ("anonymousUser".equals(userId) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인 후 이용 가능한 기능입니다.");
        }
        // 1. 사용자 조회 (예시)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        // 2. 패스워드 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }
        // 3. 삭제 수행
        userRepository.delete(user);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }


    

}
