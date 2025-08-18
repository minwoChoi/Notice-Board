package com.example.demo.dto.post.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreatRequest {
    
    private String category;

    private String title;
    
    private String content;

    private String photo;
}
