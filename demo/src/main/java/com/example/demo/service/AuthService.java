package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;
import com.example.demo.dto.user.request.UserReisterRequest;
import com.example.demo.dto.user.response.UserInfoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;  

    // (1) 아직 토큰 기반 인증을 쓰지 않는다면, 임시로라도 아래처럼 구현
    public String extractUserIdFromToken(String token) {
        // JWT 도입 전까지는 사용 금지(그냥 예외처리)
        throw new UnsupportedOperationException("아직 토큰 인증 미구현");
    }

    // (2) 내 정보 조회 기능(이미 존재해야 UserController에서 쓸 수 있음)
    public UserInfoResponse getUserInfo(String userId) {
        return userRepository.findByUserId(userId)
                .map(UserInfoResponse::fromEntity)
                .orElse(null);
    }
    
    //회원가입
    public void register(UserReisterRequest request) {   
        //아이디 중복 체크
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        //닉네임 중복 체크
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        //비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //엔티티 생성 및 값 복사
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNickname(request.getNickname());
        user.setAuthority(request.getAuthority());
        user.setProfilePicture(request.getProfilePicture());

        //저장
        userRepository.save(user);
    }
}
