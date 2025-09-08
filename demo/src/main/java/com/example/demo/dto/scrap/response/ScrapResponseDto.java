package com.example.demo.dto.scrap.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScrapResponseDto {

    private Long scrapId;
    private Long postId;
    private String postTitle;
    private String postContent;
    private LocalDateTime postCreatedDate;
    private String authorNickname; // 게시물 작성자 닉네임

    // 👇 1. 게시물 자체의 사진 URL 필드 추가
    private String postPhotoUrl;

    // 👇 2. 게시물 작성자의 프로필 사진 URL 필드 추가
    private String authorProfilePictureUrl;

    // 생성자나 빌더를 사용한다면 이 필드들을 추가해야 합니다.
    // 여기서는 서비스 레이어에서 직접 값을 설정하는 것을 기준으로 설명하겠습니다.
}