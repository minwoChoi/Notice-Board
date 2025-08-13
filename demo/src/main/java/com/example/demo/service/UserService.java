package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.user.requset.UserEditRequset;
import com.example.demo.dto.user.requset.UserReisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;  

    //프로필수정
    public void editProfile(String userId, UserEditRequset editRequest) {
    User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    // null이 아닌 값만 업데이트
    if (editRequest.getPassword() != null) user.setPassword(passwordEncoder.encode(editRequest.getPassword()));
    if (editRequest.getEmail() != null) user.setEmail(editRequest.getEmail());
    if (editRequest.getName() != null) user.setName(editRequest.getName());
    if (editRequest.getPhoneNumber() != null) user.setPhoneNumber(editRequest.getPhoneNumber());
    if (editRequest.getNickname() != null) user.setNickname(editRequest.getNickname());
    if (editRequest.getProfilePicture() != null) user.setProfilePicture(editRequest.getProfilePicture());

    userRepository.save(user);
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
