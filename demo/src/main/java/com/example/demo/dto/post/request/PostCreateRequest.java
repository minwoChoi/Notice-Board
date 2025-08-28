package com.example.demo.dto.post.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequest {
    
    private Long categoryId;

    private String title;
    
    private String content;

    private byte[] photo;
}
