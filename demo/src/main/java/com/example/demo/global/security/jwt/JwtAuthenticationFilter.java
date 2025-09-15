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
     * 이 필터의 핵심 로직입니다.
     * 모든 요청에 대해 토큰을 확인하여 "선택적 인증"을 수행합니다.
     * * - 유효한 토큰이 있는 경우: 사용자가 누구인지 식별하여 인증 정보를 SecurityContext에 저장합니다.
     * - 토큰이 없거나 유효하지 않은 경우: 아무것도 하지 않고 다음 필터로 넘깁니다. 요청은 '익명 사용자'로 처리됩니다.
     * * 최종적인 접근 허용/거부(인가)는 SecurityConfig의 설정이 담당합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 1. 요청(Header 또는 Cookie)으로부터 토큰을 추출합니다.
        String token = resolveToken(request);

        // 2. 토큰이 존재하고 유효한 경우에만 인증 정보를 SecurityContext에 설정합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰으로부터 Authentication 객체를 받아옵니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // SecurityContext에 인증 정보를 설정합니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: '{}', uri: {}", authentication.getName(), requestURI);
        } else {
            // 토큰이 없거나 유효하지 않으면 익명 사용자로 처리됩니다.
            log.debug("No valid JWT token found, proceeding as anonymous, uri: {}", requestURI);
        }

        // 3. 다음 필터로 제어를 넘깁니다.
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 토큰을 추출하는 통합 메서드입니다.
     * 1순위로 Authorization 헤더를 확인하고, 없는 경우 Cookie를 확인합니다.
     *
     * @param request 현재 HTTP 요청
     * @return 추출된 JWT 토큰 문자열, 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        // 앱 클라이언트를 위해 헤더에서 먼저 토큰을 확인합니다.
        String tokenFromHeader = resolveTokenFromHeader(request);
        if (tokenFromHeader != null) {
            return tokenFromHeader;
        }

        // 헤더에 토큰이 없다면 웹 클라이언트를 위해 쿠키를 확인합니다.
        return resolveTokenFromCookie(request);
    }

    /**
     * 'Authorization' 헤더에서 'Bearer ' 접두사를 제거하고 토큰 값만 추출합니다.
     *
     * @param request 현재 HTTP 요청
     * @return 추출된 JWT 토큰 문자열, 없으면 null
     */
    private String resolveTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 'StringUtils.hasText'는 null, 빈 문자열, 공백으로만 된 문자열을 모두 체크합니다.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("Token resolved from Authorization Header");
            return bearerToken.substring(7); // "Bearer " 다음부터의 문자열을 반환합니다.
        }
        return null;
    }

    /**
     * 요청에 포함된 쿠키 배열에서 'accessToken'이라는 이름의 쿠키 값을 추출합니다.
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