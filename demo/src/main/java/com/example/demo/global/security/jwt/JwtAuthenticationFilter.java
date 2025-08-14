package com.example.demo.global.security.jwt;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import jakarta.servlet.http.Cookie;


// 클라이언트 요청시 JWT 인증을 하기 위해 설치하는 커스텀 필터
// UsernamePasswordAuthenticationFilter 이전에 실행
// 클라이언트에서 들어오는 요청에서 JWT 토큰 처리
// => 유효한 토큰이면 토큰의 인증 정보를 SecurityContext에 저장하여 인증된 요청을 처리할 수 있도록

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilter {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// Request Header에서 JWT 토큰 추출
		String accessToken = resolveToken((HttpServletRequest) request);

		// accessToken 유효성 검사하기
		if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
			// 토큰이 유효할 경우, 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장함
			Authentication authentication = jwtTokenProvider.getAuthentication(accessToken); // 토큰에 있는 정보 꺼내기
			SecurityContextHolder.getContext().setAuthentication(authentication); // 현재 실행 중인 스레드에 인증 정보를 저장
		}
		// 토큰이 없거나 유효하지 않아도, 보안 설정(permitAll/인증 요구)에 따라 처리되도록 다음 필터로 진행
		chain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		// 1) 쿠키에서 토큰 추출
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("accessToken".equals(cookie.getName())) { // 쿠키 이름이 accessToken인 경우
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	
}