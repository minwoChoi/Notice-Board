package com.example.demo.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ 1. 토큰 검사를 건너뛸 경로 목록을 정의합니다.
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/auth/login",
            "/auth/reissue",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/users/", // POST 회원가입
            "/users/checkId", // GET 아이디 중복 확인
            "/users/checkNickname", // GET 닉네임 중복 확인
            "/posts/", // GET 게시글 목록 조회
            "/posts/{id}" // GET 게시글 상세 조회
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // ✅ 2. 현재 요청 경로가 EXCLUDE_URLS 목록에 있는지 확인합니다.
        String path = request.getRequestURI();
        if (EXCLUDE_URLS.stream().anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, path))) {
            // 목록에 있다면, 토큰 검사 없이 다음 필터로 바로 통과시킵니다.
            log.debug("JWT Filter an bypass for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // --- 여기서부터는 기존 로직과 동일합니다 ---
        // (단, EXCLUDE_URLS에 없는 경로에 대해서만 실행됩니다)

        String token = resolveToken(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: '{}', uri: {}", authentication.getName(), path);
        } else {
            log.debug("No valid JWT token found, uri: {}", path);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.info("Token resolved from Authorization Header");
            return bearerToken.substring(7);
        }
        return null;
    }
}