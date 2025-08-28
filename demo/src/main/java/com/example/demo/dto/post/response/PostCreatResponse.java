package com.example.demo.dto.post.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreatResponse {
    private Long postId;
    private String title;
    private String content;
    private byte[] photo;
    private String nickname;
    private LocalDateTime createdDate;
    private int likeCount;
    private int viewCount;

}
