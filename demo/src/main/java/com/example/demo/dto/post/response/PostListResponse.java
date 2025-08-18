package com.example.demo.dto.post.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostListResponse {
    private int postId;
    private String category;
    private String title;
    private String content;
    private String photo;
    private String username; 
    private LocalDateTime createdDate;
    private int likeCount;
    private int viewCount;
}
