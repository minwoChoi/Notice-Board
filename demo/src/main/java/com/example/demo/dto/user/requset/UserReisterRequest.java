package com.example.demo.dto.user.requset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReisterRequest {
    @Schema(description = "유저 아이디", example = "testuser1")
    private String userId;

    @Schema(description = "비밀번호", example = "testpassword123")
    private String password;

    @Schema(description = "eamil", example = "test@naver.com")
    private String email;

    @Schema(description = "이름", example = "testpassword123")
    private String name;

    @Schema(description = "전화번호", example = "testpassword123")
    private String phoneNumber;

    @Schema(description = "닉네임", example = "testpassword123")
    private String nickname;

    private Boolean authority;

    private String profilePicture;
    
    public UserReisterRequest() {}
}
