package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.user.response.UserLoginResponse;
import com.example.demo.dto.user.request.UserLoginRequest;
import com.example.demo.global.security.jwt.JwtToken;
import com.example.demo.global.security.jwt.JwtTokenProvider;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody UserLoginRequest loginRequest,
            HttpServletResponse response // 쿠키 설정을 위해 파라미터 추가
    ) {
        System.out.println("--- 로그인 요청 수신 ---");
        System.out.println("ID: " + loginRequest.getUserId());
        System.out.println("Password: " + loginRequest.getPassword()); // 실제 운영 환경에서는 비밀번호를 로그로 남기지 않는 것이 좋습니다.
        System.out.println("Client Type: " + loginRequest.getClientType());
        System.out.println("-----------------------");
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
        
        Optional<User> userOptional = userRepository.findByUserId(loginRequest.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // --- 클라이언트 타입에 따른 분기 처리 ---
        if (loginRequest.getClientType() == 0) { // Web 클라이언트인 경우
            // Access Token 쿠키 설정
            Cookie accessTokenCookie = createCookie("accessToken", jwtToken.getAccessToken(), (int) (JwtTokenProvider.ACCESS_TOKEN_EXPIRE_TIME / 1000));
            response.addCookie(accessTokenCookie);

            // Refresh Token 쿠키 설정
            Cookie refreshTokenCookie = createCookie("refreshToken", jwtToken.getRefreshToken(), (int) (JwtTokenProvider.REFRESH_TOKEN_EXPIRE_TIME / 1000));
            response.addCookie(refreshTokenCookie);
            
            // 1. 프로필 사진 URL 생성 (사진이 있을 경우에만)
            String profilePictureUrl = null;
            if (user.getProfilePicture() != null && user.getProfilePicture().length > 0) {
                profilePictureUrl = "/users/" + user.getUserId() + "/photo";
            }

            // 2. UserLoginResponse DTO 빌드
            UserLoginResponse responseDto = UserLoginResponse.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .authorProfilePictureUrl(profilePictureUrl)
                    .build();

            return ResponseEntity.ok(responseDto);

        } else { // App 클라이언트(또는 그 외)인 경우
            // 기존 방식대로 응답 본문에 토큰 전체를 포함하여 반환
            return ResponseEntity.ok(jwtToken);
        }
    }
    // [추가] 보안 설정을 적용한 쿠키 생성 헬퍼 메서드
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가 (XSS 방어)
        cookie.setSecure(false);   // 개발 중에는 false, 배포 시에는 true로 변경 (HTTPS에서만 전송)
        cookie.setPath("/");      // 모든 경로에서 쿠키 사용 가능
        cookie.setMaxAge(maxAge); // 쿠키 만료 시간 설정 (초 단위)
        return cookie;
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

