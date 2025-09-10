package com.example.demo.dto.post.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostEditRequest {
    private Long categoryId;

    private String title;
    
    private String content;

    // private String photo;
}
