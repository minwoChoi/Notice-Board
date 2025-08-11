package com.example.demo.dto.user.requset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEditRequset {
    // 비밀번호 변경(필요에 따라)
    @Schema(description = "변경할 비밀번호", example = "newpassword123")
    private String password;

    @Schema(description = "변경할 이메일", example = "test2@naver.com")
    private String email;

    @Schema(description = "변경할 이름", example = "홍길동")
    private String name;

    @Schema(description = "변경할 전화번호", example = "010-9876-5432")
    private String phoneNumber;

    @Schema(description = "변경할 닉네임", example = "newnickname")
    private String nickname;

    @Schema(description = "변경할 프로필 사진 URL", example = "https://example.com/myprofile.jpg")
    private String profilePicture;


    public UserEditRequset() {}
}
