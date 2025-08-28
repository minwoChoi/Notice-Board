package com.example.demo.dto.post.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostEditResponse {
    private Long postId;          // 게시글 고유 ID
    private String categoryName;      // 카테고리
    private String title;         // 제목
    private String content;       // 내용
    private byte[] photo;         // 사진 URL 등
    private String username;    // 작성자 이름(또는 사용자명)
    private LocalDateTime createdDate;   // 생성일시
    private int likeCount;        // 좋아요 수
    private int viewCount;        // 조회수
}
