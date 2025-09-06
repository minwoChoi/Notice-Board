package com.example.demo.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseCookie; // 💡 주석 처리
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder; // 💡 주석 처리
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import com.example.demo.repository.UserRepository;

// import jakarta.servlet.http.HttpServletResponse; // 💡 주석 처리

import com.example.demo.dto.user.request.UserLoginRequest;
// import com.example.demo.dto.user.response.UserLoginResponse; // 💡 주석 처리
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
    public ResponseEntity<JwtToken> login( // 💡 반환 타입을 JwtToken으로 변경
            @RequestBody UserLoginRequest loginRequest
            /*, HttpServletResponse httpResponse */ // 💡 HttpServletResponse 파라미터 주석 처리
    ) {
        // 1) 유저 존재 여부 확인
        Optional<User> userOptional = userRepository.findByUserId(loginRequest.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        User user = userOptional.get();
    
        // 2) 비밀번호 검사
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        // 3) Authentication 객체 만들기
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
    
        // 4) JwtTokenProvider로 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
    
        /* 💡 쿠키 및 기존 응답 본문 생성 로직을 모두 주석 처리 */
        /*
        // 5) Access Token 쿠키 세팅
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtToken.getAccessToken())
                .httpOnly(true)     // JavaScript 접근 불가(XSS 방어)
                .secure(false)       // HTTPS 에서만 전송 (개발 중이면 false 가능)
                .path("/")
                .maxAge(60 * 60)    // 1시간
                .sameSite("Lax") // CSRF 방지
                .build();
    
        // 6) 쿠키를 response 헤더에 추가
        httpResponse.addHeader("Set-Cookie", accessCookie.toString());
    
        // 7) 바디에는 사용자 정보만
        UserLoginResponse body = UserLoginResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    
        return ResponseEntity.ok(body);
        */

        // 💡 생성된 JwtToken 객체 전체를 응답 본문에 담아 반환
        return ResponseEntity.ok(jwtToken);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout( // 💡 반환 타입 및 파라미터 변경
            @RequestHeader("Authorization") String accessToken
            /* HttpServletResponse response */) {

        /* 💡 기존 로그아웃 로직 전체를 주석 처리 */
        /*
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
        */

        // 💡 클라이언트 측에서 토큰을 삭제하므로 서버는 별도 처리 없이 성공 응답
        return ResponseEntity.ok().build();
    }

    // 💡 토큰 재발급을 위한 API 새로 추가
    @PostMapping("/reissue")
    public ResponseEntity<JwtToken> reissue(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        } else {
            return ResponseEntity.badRequest().build();
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwtTokenProvider.getUserNameFromToken(refreshToken);
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUserId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        JwtToken newJwtToken = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(newJwtToken);
    }
}

