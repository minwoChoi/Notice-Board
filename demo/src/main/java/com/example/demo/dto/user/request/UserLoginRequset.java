package com.example.demo.dto.user.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequset {
    @Schema(description = "유저 아이디", example = "1901614")
    private String userId;

    @Schema(description = "비밀번호", example = "asldn417")
    private String password;
    
    public UserLoginRequset() {}
}
