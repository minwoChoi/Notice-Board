// com/example/demo/global/security/jwt/JwtAuthenticationFilter.java

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 클라이언트의 모든 요청에 대해 JWT 토큰을 검사하고 인증 정보를 SecurityContext에 설정하는 커스텀 필터입니다.
 * 웹(Cookie)과 앱(Authorization Header) 클라이언트의 인증 방식을 모두 지원합니다.
 * OncePerRequestFilter를 상속하여 요청 당 한 번만 필터가 실행되도록 보장합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 실제 필터링 로직을 수행하는 메소드입니다.
     * 요청에서 토큰을 추출하고, 유효성을 검사한 뒤 SecurityContext에 인증 정보를 저장합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 요청 URI 로깅을 위해 추출
        String requestURI = request.getRequestURI();
        
        // ★★★ 웹/앱 요청을 모두 처리하기 위해 통합된 resolveToken 메소드를 호출합니다.
        String token = resolveToken(request);
        
        // 토큰이 존재하고 유효한 경우
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰으로부터 Authentication 객체를 받아옵니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // SecurityContext에 인증 정보를 설정합니다. 이로써 해당 요청은 인증된 사용자의 요청으로 처리됩니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: '{}', uri: {}", authentication.getName(), requestURI);
        } else {
            // 유효한 토큰이 없는 경우 (비로그인 사용자 또는 토큰 만료)
            log.debug("No valid JWT token found, uri: {}", requestURI);
        }

        // 다음 필터로 요청과 응답을 전달합니다.
        filterChain.doFilter(request, response);
    }

    /**
     * ★★★ [기능 추가] HttpServletRequest에서 토큰을 추출하는 통합 메소드입니다.
     * 1순위로 Authorization 헤더를 확인하고, 없는 경우 Cookie를 확인합니다.
     *
     * @param request 현재 HTTP 요청
     * @return 추출된 JWT 토큰 문자열, 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        // 1순위: 헤더에서 토큰 확인 (앱 클라이언트)
        String tokenFromHeader = resolveTokenFromHeader(request);
        if (tokenFromHeader != null) {
            return tokenFromHeader;
        }

        // 2순위: 헤더에 토큰이 없으면 쿠키에서 확인 (웹 클라이언트)
        return resolveTokenFromCookie(request);
    }

    /**
     * 'Authorization' 헤더에서 Bearer 토큰을 추출합니다.
     *
     * @param request 현재 HTTP 요청
     * @return 추출된 JWT 토큰 문자열, 없으면 null
     */
    private String resolveTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("Token resolved from Authorization Header");
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 쿠키 배열에서 'accessToken'을 가진 쿠키의 값을 추출합니다.
     *
     * @param request 현재 HTTP 요청
     * @return 추출된 JWT 토큰 문자열, 없으면 null
     */
    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                log.debug("Token resolved from Cookie");
                return cookie.getValue();
            }
        }
        return null;
    }
}