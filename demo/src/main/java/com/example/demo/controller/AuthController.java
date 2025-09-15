// com/example/demo/controller/AuthController.java

package com.example.demo.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie; // 👇 1. ResponseCookie를 임포트합니다.
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
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
import com.example.demo.global.util.DeviceUtil; // DeviceUtil 임포트
import jakarta.servlet.http.HttpServletRequest; // HttpServletRequest 임포트
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
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
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Request User-Agent: {}", request.getHeader("User-Agent"));
        
        String deviceType = DeviceUtil.getDeviceType(request);
        log.info("[Login Attempt] User-ID: [{}], Device-Type: [{}]", 
                 loginRequest.getUserId(), 
                 deviceType);

        User user = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("[Login Failed] Incorrect password for User-ID: [{}]", loginRequest.getUserId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        if (loginRequest.getClientType() == 0) { // Web 클라이언트: 쿠키 방식
            // 👇 3. addHeader를 사용하여 쿠키를 응답에 추가합니다.
            response.addHeader("Set-Cookie", createCookieHeader("accessToken", jwtToken.getAccessToken(), (int) (JwtTokenProvider.ACCESS_TOKEN_EXPIRE_TIME / 1000)));
            response.addHeader("Set-Cookie", createCookieHeader("refreshToken", jwtToken.getRefreshToken(), (int) (JwtTokenProvider.REFRESH_TOKEN_EXPIRE_TIME / 1000)));
            
            String profilePictureUrl = (user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                ? "/users/" + user.getUserId() + "/photo" : null;
            
            UserLoginResponse responseDto = UserLoginResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .authorProfilePictureUrl(profilePictureUrl)
                .build();
            
            return ResponseEntity.ok(responseDto);

        } else {
            return ResponseEntity.ok(jwtToken);
        }
    }
    
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenFromCookie,
            HttpServletResponse response
    ) {
        String refreshToken;
        boolean isAppClient = false;
        String clientType = "Web"; // 로그용 클라이언트 타입 변수

        // 1. 토큰 추출
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            refreshToken = bearerToken.substring(7);
            isAppClient = true;
            clientType = "App"; // 클라이언트 타입 변경
        } else if (StringUtils.hasText(refreshTokenFromCookie)) {
            refreshToken = refreshTokenFromCookie;
        } else {
            // ▼▼▼ 실패 로그 1: 토큰이 아예 없는 경우
            log.warn("[토큰 재발급 실패] 요청에 리프레시 토큰이 없습니다. (Header, Cookie 모두 없음)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing.");
        }
        
        // 2. 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            // ▼▼▼ 실패 로그 2: 토큰이 유효하지 않은 경우
            log.warn("[토큰 재발급 실패] 유효하지 않은 리프레시 토큰입니다. Token: {}", refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token.");
        }

        // 3. 토큰에서 사용자 정보 추출 및 DB 조회
        String userId = jwtTokenProvider.getUserNameFromToken(refreshToken);
        User user = userRepository.findByUserId(userId)
                .orElse(null); // orElseThrow 대신 orElse(null)로 변경하여 직접 처리

        if (user == null) {
            // ▼▼▼ 실패 로그 3: 토큰은 유효하지만 해당 유저가 DB에 없는 경우
            log.error("[토큰 재발급 실패] 토큰의 사용자 정보를 DB에서 찾을 수 없습니다. UserId: {}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User from token not found.");
        }
        
        // 4. 새 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        JwtToken newJwtToken = jwtTokenProvider.generateToken(authentication);

        // ▼▼▼ 성공 로그 ▼▼▼
        log.info("[토큰 재발급 성공] 사용자: '{}', 클라이언트 타입: {}", userId, clientType);

        // 5. 클라이언트 타입에 따라 응답 분기
        if (isAppClient) {
            return ResponseEntity.ok(newJwtToken);
        } else {
            response.addHeader("Set-Cookie", createCookieHeader("accessToken", newJwtToken.getAccessToken(), (int) (JwtTokenProvider.ACCESS_TOKEN_EXPIRE_TIME / 1000)));
            response.addHeader("Set-Cookie", createCookieHeader("refreshToken", newJwtToken.getRefreshToken(), (int) (JwtTokenProvider.REFRESH_TOKEN_EXPIRE_TIME / 1000)));
            return ResponseEntity.ok().build();
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader)) {
            // App client
        } else { // Web client
            // 👇 3. addHeader를 사용하여 쿠키를 응답에 추가합니다. (만료시켜서 삭제)
            response.addHeader("Set-Cookie", createCookieHeader("accessToken", null, 0));
            response.addHeader("Set-Cookie", createCookieHeader("refreshToken", null, 0));
        }

        return ResponseEntity.ok().build();
    }

    // 👇 2. 헬퍼 메서드가 ResponseCookie를 사용해 String을 반환하도록 수정합니다.
    private String createCookieHeader(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(false) // TODO: 배포 시 true로 변경
            .path("/")
            .maxAge(maxAge)
            .sameSite("Lax") // 👈 SameSite 정책을 여기서 설정합니다.
            .build()
            .toString();
    }
}