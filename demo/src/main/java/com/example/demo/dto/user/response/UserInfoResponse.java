package com.example.demo.dto.user.response;

import com.example.demo.model.User;
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
    private byte[] profilePicture;

    // User 엔티티 → DTO 변환용 생성자 or 팩토리 메서드
    public static UserInfoResponse fromEntity(User user) {
        UserInfoResponse dto = new UserInfoResponse();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setNickname(user.getNickname());
        dto.setAuthority(user.getAuthority());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
}
