package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.user.request.UserEditRequset;
import com.example.demo.dto.user.request.UserReisterRequest; // <-- Reister 오타는 그대로 두었습니다. DTO 파일 이름과 일치해야 합니다.
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j import 추가

@Slf4j // <-- 로그를 위한 어노테이션 추가
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public byte[] getProfilePictureByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));
        return user.getProfilePicture();
    }

    //프로필수정
    @Transactional
    public void editProfile(String userId, UserEditRequset editRequest) {
        User user = userRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // null이 아닌 값만 업데이트
        if (editRequest.getPassword() != null) user.setPassword(passwordEncoder.encode(editRequest.getPassword()));
        if (editRequest.getEmail() != null) user.setEmail(editRequest.getEmail());
        if (editRequest.getName() != null) user.setName(editRequest.getName());
        if (editRequest.getPhoneNumber() != null) user.setPhoneNumber(editRequest.getPhoneNumber());
        if (editRequest.getNickname() != null) user.setNickname(editRequest.getNickname());
        if (editRequest.getProfilePicture() != null) {
            user.setProfilePicture(editRequest.getProfilePicture());
        }
    }
    
    //회원가입
    @Transactional 
    public void register(UserReisterRequest request) {   
        log.info("[register] 회원가입 서비스 시작. 요청된 ID: {}", request.getUserId());

        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNickname(request.getNickname());
        user.setAuthority(request.getAuthority());
        user.setProfilePicture(request.getProfilePicture());

        // ▼▼▼ [핵심] DB 저장을 위한 코드 추가 ▼▼▼
        try {
            log.info("[register] userRepository.save() 호출 직전. User ID: {}", user.getUserId());
            userRepository.save(user);
            log.info("[register] userRepository.save() 호출 성공! DB 저장 완료. User ID: {}", user.getUserId());
        } catch (Exception e) {
            log.error("[register] CRITICAL: DB 저장 중 예외 발생!", e);
            // 예외를 다시 던져서 트랜잭션이 롤백되도록 합니다.
            throw new RuntimeException("DB 저장 중 오류가 발생했습니다.", e);
        }
    }
}