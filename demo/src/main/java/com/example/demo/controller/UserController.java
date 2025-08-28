package com.example.demo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;

import com.example.demo.dto.scrap.response.ScrapResponseDto;
import com.example.demo.dto.user.request.UserDeleteRequest;
import com.example.demo.dto.user.request.UserEditRequset;
import com.example.demo.dto.user.request.UserReisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.ScrapService;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final AuthService userAuthService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ScrapService scrapService;

    // 내 스크랩 목록 조회
    @GetMapping("/me/scraps")
    public ResponseEntity<List<ScrapResponseDto>> getMyScraps(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(scrapService.getMyScraps(userId));
    }

    // 회원가입 (이미지 포함)
    @PostMapping(value = "/", consumes = { "multipart/form-data" })
    public ResponseEntity<String> register(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("nickname") String nickname,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) throws IOException {

        UserReisterRequest request = new UserReisterRequest();
        request.setUserId(userId);
        request.setPassword(password);
        request.setEmail(email);
        request.setName(name);
        request.setPhoneNumber(phoneNumber);
        request.setNickname(nickname);
        request.setAuthority(true); // 기본 권한 설정

        if (profilePicture != null && !profilePicture.isEmpty()) {
            request.setProfilePicture(profilePicture.getBytes());
        }

        try {
            userService.register(request);
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        String userId = authentication.getName();
        var userInfo = userAuthService.getUserInfo(userId);
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("해당 유저(" + userId + ")를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(userInfo);
    }

    // 프로필 수정 (이미지 포함)
    @PatchMapping(value = "/", consumes = { "multipart/form-data" })
    public ResponseEntity<String> editProfile(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication authentication) throws IOException {

        String userId = authentication.getName();

        UserEditRequset editRequest = new UserEditRequset();
        editRequest.setPassword(password);
        editRequest.setEmail(email);
        editRequest.setName(name);
        editRequest.setPhoneNumber(phoneNumber);
        editRequest.setNickname(nickname);
        
        if (profilePicture != null && !profilePicture.isEmpty()) {
            editRequest.setProfilePicture(profilePicture.getBytes());
        }

        try {
            userService.editProfile(userId, editRequest);
            return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe(@RequestBody UserDeleteRequest request, Authentication authentication) {
        String userId = authentication.getName();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }
        
        userRepository.delete(user);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
