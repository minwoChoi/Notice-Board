package com.example.demo.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import com.example.demo.dto.user.requset.UserLoginRequset;
import com.example.demo.dto.user.response.UserLoginResponse;
import com.example.demo.model.User;
import com.example.demo.global.security.jwt.JwtToken;
import com.example.demo.global.security.jwt.JwtTokenProvider;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @RequestBody UserLoginRequset loginRequest,
            HttpServletResponse httpResponse
    ) {
        // 1) 유저 존재 여부 확인
        Optional<User> userOptional = userRepository.findByUserId(loginRequest.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    
        User user = userOptional.get();
    
        // 2) 비밀번호 검사
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    
        // 3) Authentication 객체 만들기
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
    
        // 4) JwtTokenProvider로 토큰 생성 (여기서 Refresh는 Redis에 저장됨)
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
    
        // 5) Access Token 쿠키 세팅
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtToken.getAccessToken())
                .httpOnly(true)     // JavaScript 접근 불가(XSS 방어)
                .secure(true)       // HTTPS 에서만 전송 (개발 중이면 false 가능)
                .path("/")
                .maxAge(60 * 60)    // 1시간
                .sameSite("Strict") // CSRF 방지
                .build();
    
        // 6) 쿠키를 response 헤더에 추가
        httpResponse.addHeader("Set-Cookie", accessCookie.toString());
    
        // 7) 바디에는 사용자 정보만
        UserLoginResponse body = UserLoginResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    
        return ResponseEntity.ok(body);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {


        // 인증된 유저 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // 비로그인 접근 방어
        if ("anonymousUser".equals(userId) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인 후 이용 가능한 기능입니다.");
        }

        // Redis에서 Refresh Token 삭제 (추가)
        jwtTokenProvider.deleteRefreshToken(userId);

        // Access Token 쿠키를 삭제하기 위해 만료된 쿠키 설정
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // 즉시 만료
                .sameSite("Strict")
                .build();

        // 클라이언트로 삭제 쿠키 전달
        response.addHeader("Set-Cookie", deleteAccessCookie.toString());

            return ResponseEntity.ok("로그아웃이 완료되었습니다.");
        }
}
