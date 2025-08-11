package com.example.demo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponse {
    private String grantType;    // Bearer
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String nickname;
}
