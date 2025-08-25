package com.example.demo.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 1. OncePerRequestFilter 상속

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();

        // 1. Request에서 JWT 토큰 추출 (헤더 또는 쿠키)
        String token = resolveToken(request);
        
        // 2. 토큰 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효할 경우, 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: '{}', uri: {}", authentication.getName(), requestURI);
        } else {
            log.debug("No valid JWT token found, uri: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header의 'Authorization' 필드 또는 쿠키에서 토큰 정보를 추출합니다.
     * @param request HttpServletRequest 객체
     * @return 추출된 토큰 문자열, 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        // 2. Authorization 헤더에서 토큰 추출 (Bearer 방식)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.info("Token resolved from Authorization Header");
            return bearerToken.substring(7); // "Bearer " 다음의 토큰 값만 반환
        }

        // 3. 헤더에 토큰이 없다면 쿠키에서 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    log.info("Token resolved from Cookie");
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}