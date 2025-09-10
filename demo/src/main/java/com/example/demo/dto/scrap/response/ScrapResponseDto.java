// src/main/java/com/example/demo/dto/scrap/response/ScrapResponseDto.java

package com.example.demo.dto.scrap.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScrapResponseDto {

    private Long scrapId;
    private Long postId;
    private Long categoryId;
    private String postTitle;
    private String postContent;
    private LocalDateTime postCreatedDate;
    private String authorNickname;
    private String postPhotoUrl;
    private String authorProfilePictureUrl;

    private int likeCount;
    private int viewCount;
    private int commentCount;
}