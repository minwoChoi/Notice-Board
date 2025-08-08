package com.example.demo.dto.user.requset;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequset {
    @Schema(description = "유저 아이디", example = "testuser1")
    private String userId;

    @Schema(description = "비밀번호", example = "testpassword123")
    private String password;
    
    public UserLoginRequset() {}
}
