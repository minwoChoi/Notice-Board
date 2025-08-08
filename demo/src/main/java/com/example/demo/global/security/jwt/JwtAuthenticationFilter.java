// package com.example.demo.global.security.jwt;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// @Component
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     // JWT 토큰 유효성 검사 및 인증정보 등록 구현 필요 (아래는 예시)
//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain)
//             throws ServletException, IOException {
//         // 예시: JWT 헤더 검증 및 인증 컨텍스트에 등록
//         // 1. 헤더에서 토큰 추출
//         // 2. 토큰 유효성 검사
//         // 3. 인증정보(SecContext) 등록

//         filterChain.doFilter(request, response); // 필터 진행 (반드시!)
//     }
// }
