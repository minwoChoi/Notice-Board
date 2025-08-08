// package com.example.demo.global.security.jwt;

// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.stereotype.Component;
// import com.example.demo.repository.RedisDao;


// import java.security.Key;
// import java.util.Base64;

// @Component
// public class JwtTokenProvider {

//     private final Key key;
//     private final UserDetailsService userDetailsService;
//     private final RedisDao redisDao;

//     private static final String GRANT_TYPE = "Bearer";

//     private final long ACCESS_TOKEN_EXPIRE_TIME;
//     private final long REFRESH_TOKEN_EXPIRE_TIME;

//     public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
//                             @Value("${jwt.access-token.expire-time}") long accessTokenExpireTime,
//                             @Value("${jwt.refresh-token.expire-time}") long refreshTokenExpireTime,
//                             UserDetailsService userDetailsService,
//                             RedisDao redisDao) {
//         byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
//         this.key = Keys.hmacShaKeyFor(keyBytes);
//         this.ACCESS_TOKEN_EXPIRE_TIME = accessTokenExpireTime;
//         this.REFRESH_TOKEN_EXPIRE_TIME = refreshTokenExpireTime;
//         this.userDetailsService = userDetailsService;
//         this.redisDao = redisDao;
//     }

//     // 추가 메서드 구현
// }
