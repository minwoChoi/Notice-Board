package com.example.demo.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;

    public static final String GRANT_TYPE = "Bearer";
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 60분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    // ▼▼▼ 변경된 부분: RedisDao 의존성 제거 ▼▼▼
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 로그인 시 토큰 생성 (AccessToken + RefreshToken) */
    public JwtToken generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        String username = authentication.getName();

        // AccessToken 생성
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = generateAccessToken(username, authorities, accessTokenExpire);

        // RefreshToken 생성
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(username, refreshTokenExpire);

        // ▼▼▼ 변경된 부분: 생성된 토큰들을 클라이언트에게 모두 전달 ▼▼▼
        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** 토큰 재발급 */
    // ▼▼▼ 새로운 부분: Redis 없이 토큰 재발급을 처리하는 메서드 ▼▼▼
    public JwtToken reissueToken(String refreshToken, UserDetailsService userDetailsService) {
        // 1. Refresh Token 검증
        if (!validateToken(refreshToken)) {
            throw new JwtException("Invalid Refresh Token");
        }

        // 2. Refresh Token에서 사용자 이름(username) 추출
        String username = getUserNameFromToken(refreshToken);

        // 3. UserDetailsService를 통해 사용자 정보 다시 로드 (권한 등 최신 정보 반영)
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        
        // 4. 새로운 Access Token 및 Refresh Token 생성 (Refresh Token Rotation)
        return generateToken(authentication);
    }


    /** AccessToken으로 Authentication 객체 생성 */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /** JWT 파싱 (만료된 토큰도 내용 확인 가능하도록) */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
        }
        return false;
    }

    /** 토큰에서 username 추출 */
    public String getUserNameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    /** AccessToken 생성 */
    private String generateAccessToken(String username, String authorities, Date expireDate) {
        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** RefreshToken 생성 */
    private String generateRefreshToken(String username, Date expireDate) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}