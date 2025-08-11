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
import com.example.demo.repository.RedisDao;

import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final RedisDao redisDao; // RefreshToken 저장을 위해 Redis 사용

    private static final String GRANT_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 60분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 ; // 1일

    // application.properties에서 secret 값 가져와서 secretKey 사용하기
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            RedisDao redisDao) {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisDao = redisDao;
    }

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하기
    public JwtToken generateToken(Authentication authentication) {
        //권하 거자요기
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

        // Redis에 RefreshToken 넣기
        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // AccessToken & RefreshToken 재발급 할 때 (UserDetailsService를 파라미터로 받음)
    public JwtToken generateTokenWithRefreshToken(String username, UserDetailsService userDetailsService) {
        long now = (new Date()).getTime();

        // UserDetailsService로 사용자 권한 정보 가져오기
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // AccessToken 생성
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = generateAccessToken(username, authorities, accessTokenExpire);

        // RefreshToken 생성
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(username, refreshTokenExpire);

        // Redis에 RefreshToken 넣기
        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보 꺼내기
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }
        
        //SimpleGrantedAuthority 객체로 변환
        //(Spring Security에서 권한을 표현하는 기본 클래스)
         

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT 토큰 복호화
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

    // 토큰 정보 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
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

    // RefreshToken 검증
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) return false;
        try {
            String username = getUserNameFromToken(token);
            String redisToken = (String) redisDao.getValues(username);
            return token.equals(redisToken);
        } catch (Exception e) {
            log.info("RefreshToken Validation Failed", e);
            return false;
        }
    }

    // 토큰에서 username 추출
    public String getUserNameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        redisDao.deleteValues(username);
    }

    // 토큰 생성 함수들
    private String generateAccessToken(String username, String authorities, Date expireDate) {
        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(String username, Date expireDate) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
