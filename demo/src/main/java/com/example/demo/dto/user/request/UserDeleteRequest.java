package com.example.demo.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDeleteRequest {

    @Schema(description = "탈퇴 전 비밀번호 확인", example = "newpassword123")
    private String password;

    public UserDeleteRequest() {}
}
