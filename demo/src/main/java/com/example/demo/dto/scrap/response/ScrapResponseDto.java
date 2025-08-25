package com.example.demo.dto.scrap.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScrapResponseDto {
    private Long scrapId;
    private Long postId;
    private String userId;  // String 타입으로 변경
    private LocalDateTime createdDate;
    private String postTitle;
}
