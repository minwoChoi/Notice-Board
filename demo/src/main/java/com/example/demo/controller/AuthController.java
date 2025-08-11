package com.example.demo.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import com.example.demo.repository.UserRepository;
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
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequset loginRequest) {
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

        // 3) Authentication 객체 만들기 (권한 세팅)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        // 4) JwtTokenProvider로 토큰 한 번에 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // 5) 로그인 성공 응답 DTO 작성
        UserLoginResponse response = UserLoginResponse.builder()
                .grantType(jwtToken.getGrantType())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // 인증된 유저 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // 비로그인 접근 방어
        if ("anonymousUser".equals(userId) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인 후 이용 가능한 기능입니다.");
        }

        // RefreshToken 삭제
        jwtTokenProvider.deleteRefreshToken(userId);

        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }
}
