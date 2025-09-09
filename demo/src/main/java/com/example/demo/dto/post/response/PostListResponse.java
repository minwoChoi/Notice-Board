package com.example.demo.dto.post.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostListResponse {
    private Long postId;
    private String userId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private String nickname;
    private LocalDateTime createdDate;
    private int viewCount;
    private int likeCount;
    private Long commentCount;
    private boolean isBlocked; 
    private String photoUrl;
    private String authorProfilePictureUrl;
    
    // JPQL 쿼리의 파라미터 순서와 정확히 일치하는 생성자
    public PostListResponse(
            Long postId, 
            String userId, 
            Long categoryId, 
            String categoryName, 
            String title, 
            String content,
            String nickname, 
            LocalDateTime createdDate, 
            int viewCount, 
            int likeCount, 
            Long commentCount,
            boolean isBlocked, // 12번째 파라미터
            String photoUrl, 
            String authorProfilePictureUrl
    ) {
        this.postId = postId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.title = title;
        this.content = content;
        this.nickname = nickname;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isBlocked = isBlocked;
        this.photoUrl = photoUrl;
        this.authorProfilePictureUrl = authorProfilePictureUrl;
    }
}