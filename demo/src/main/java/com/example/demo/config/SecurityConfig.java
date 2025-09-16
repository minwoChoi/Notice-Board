package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.global.security.jwt.JwtAuthenticationFilter;
import com.example.demo.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String[] PUBLIC_URLS = {
            "/auth/login",
            "/auth/reissue",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/users/checkId",
            "/users/checkNickname",
            "/posts/*/photo",
            "/users/*/photo"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(b -> b.disable())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())

                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                }))

                // ==========================================================
                // ★★★ 권한 규칙 순서 조정 (더 구체적인 규칙을 위로) ★★★
                // ==========================================================
                .authorizeHttpRequests(auth -> auth
                        // 1. 인증 및 권한이 필요한 API들을 먼저 정의합니다.
                        .requestMatchers("/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/reports/posts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/reports/posts/**").authenticated()
                        .requestMatchers("/users/role").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/admin/users/**").hasAuthority("ROLE_ADMIN")

                        // 2. 그 외 모든 사람이 접근 가능한 공개 API들을 정의합니다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll() // 회원가입
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll() // 게시글 조회

                        // 3. 위에서 정의하지 않은 나머지 모든 요청은 인증이 필요하도록 설정합니다.
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 내 PC에서 개발 서버를 띄우고 localhost로 접속할 때
        configuration.addAllowedOriginPattern("http://localhost:3000");
        configuration.addAllowedOriginPattern("http://127.0.0.1:3000");

        // 2. 백엔드와 같은 PC(192.168.0.172)에서 프론트엔드를 띄우고 IP
        configuration.addAllowedOriginPattern("http://192.168.0.172:3000");

        // 3. 다른 PC(192.168.0.166)에서 프론트엔드를 띄우고 접속할 때
        configuration.addAllowedOriginPattern("http://192.168.0.166:3000");

        // 4. 배포 서버 IP (유지)
        configuration.addAllowedOriginPattern("http://192.168.0.101");
        configuration.addAllowedOriginPattern("https://192.168.0.101");


        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
