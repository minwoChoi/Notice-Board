package com.example.demo.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // 💡 Cookie 임포트 주석 처리
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 클라이언트 요청 시 JWT 인증을 하기 위해 설치하는 커스텀 필터
 * OncePerRequestFilter: 요청 당 한 번만 실행되는 것을 보장
 */
// com/example/demo/global/security/jwt/JwtAuthenticationFilter.java

// ... (import 및 클래스 선언은 동일)

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String token = resolveToken(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: '{}', uri: {}", authentication.getName(), requestURI);
        } else {
            log.debug("No valid JWT token found, uri: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    // [수정] 쿠키에서도 토큰을 읽도록 resolveToken 메서드 변경
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출 (App 클라이언트용)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("Token resolved from Authorization Header");
            return bearerToken.substring(7);
        }

        // 2. 헤더에 토큰이 없다면 쿠키에서 추출 (Web 클라이언트용)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                // AccessToken만 확인합니다. RefreshToken은 재발급 용도로만 사용됩니다.
                if ("accessToken".equals(cookie.getName())) {
                    log.debug("Token resolved from Cookie");
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}