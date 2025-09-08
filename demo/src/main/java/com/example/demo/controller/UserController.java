package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.comment.response.MyCommentResponse;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.dto.scrap.response.ScrapResponseDto;
import com.example.demo.dto.user.request.UserDeleteRequest;
import com.example.demo.dto.user.request.UserEditRequset;
import com.example.demo.dto.user.request.UserReisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import com.example.demo.service.ScrapService;
import com.example.demo.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final PostService postService;
    private final CommentService commentService;

    // 내 스크랩 목록 조회
    @GetMapping("/me/scraps")
    public ResponseEntity<List<ScrapResponseDto>> getMyScraps(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(scrapService.getMyScraps(userId));
    }

    //내가 쓴 댓글 목록 조회 API 추가
    @GetMapping("/me/comments")
    public ResponseEntity<List<MyCommentResponse>> getMyComments(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commentService.findMyComments(userId));
    }

    //내가 쓴 게시물 목록 조회 API 추가
    @GetMapping("/me/posts")
    public ResponseEntity<List<PostListResponse>> getMyPosts(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(postService.findMyPosts(userId));
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
            // AuthService가 이제 URL이 포함된 UserInfoResponse를 반환합니다.
            var userInfo = userAuthService.getUserInfo(userId);
            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body("해당 유저(" + userId + ")를 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(userInfo);
        }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<byte[]> getUserProfilePhoto(@PathVariable String userId) {
        byte[] photoBytes = userService.getProfilePictureByUserId(userId);

        if (photoBytes == null || photoBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // 브라우저가 이 응답을 이미지로 해석하도록 Content-Type 설정
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 또는 IMAGE_PNG 등 이미지 타입에 맞게 설정
                .body(photoBytes);
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
