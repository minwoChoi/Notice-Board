package com.example.demo.dto.user.response;

import com.example.demo.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoResponse {
    private String userId;
    private String email;
    private String name;
    private String phoneNumber;
    private String nickname;
    private Boolean authority;
    private String profilePictureUrl;

    @JsonIgnore // 👈 JSON 직렬화 시 이 필드를 무시합니다.
    private byte[] profilePicture;

    // User 엔티티 → DTO 변환용 생성자 or 팩토리 메서드
    public UserInfoResponse(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.nickname = user.getNickname();
        this.profilePicture = user.getProfilePicture();
        // 👈 프로필 사진이 있으면 URL을 생성하고, 없으면 null을 설정합니다.
        if (user.getProfilePicture() != null && user.getProfilePicture().length > 0) {
            this.profilePictureUrl = "/users/" + user.getUserId() + "/photo";
        } else {
            this.profilePictureUrl = null;
        }
    }
}
