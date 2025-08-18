package com.example.demo.dto.post.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostEditRequest {
    private String category;

    private String title;
    
    private String content;

    private String photo;
}
