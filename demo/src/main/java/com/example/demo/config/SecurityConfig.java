package com.example.demo.config;

import com.example.demo.global.security.jwt.JwtAuthenticationFilter;
import com.example.demo.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    // 1. 공개적으로 접근 가능한 경로들을 상수로 관리
    private static final String[] PUBLIC_URLS = {
            "/auth/login",
            // Swagger UI & API Docs
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // --- 기본 설정 ---
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .csrf(AbstractHttpConfigurer::disable)      // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 2. CORS 설정 적용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 STATELESS 설정
                .formLogin(AbstractHttpConfigurer::disable) // Form Login 비활성화
                .logout(AbstractHttpConfigurer::disable)    // Logout 비활성화

                // --- 경로별 인가 규칙 설정 ---
                .authorizeHttpRequests(authorize ->
                        authorize
                                // 공개 경로 허용
                                .requestMatchers(PUBLIC_URLS).permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/").permitAll() // 회원가입
                                .requestMatchers(HttpMethod.GET, "/users/checkId").permitAll()
                                .requestMatchers(HttpMethod.GET, "/users/checkNickname").permitAll()

                                // 3. 인증이 필요한 경로 명시
                                .requestMatchers("/users/me/**").authenticated() // '/users/me/'로 시작하는 모든 경로는 인증 필요

                                // 역할(Role) 기반 접근 제어
                                .requestMatchers("/members/role").hasRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN") // 4. 경로 수정 제안

                                // 나머지 모든 요청은 인증 필요
                                .anyRequest().authenticated()
                )
                // --- 필터 추가 ---
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:3000");
        configuration.addAllowedOriginPattern("http://127.0.0.1:3000");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}