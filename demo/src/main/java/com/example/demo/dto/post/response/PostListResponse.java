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
    private String photoUrl; // 게시물 자체의 사진 URL
    private String nickname;
    private LocalDateTime createdDate;
    private int likeCount;
    private int viewCount;
    private String authorProfilePictureUrl;
    private boolean isBlocked;
    private Long commentCount;
    public PostListResponse(Long postId, String userId, Long categoryId,String categoryName, String title, String content,String nickname, 
    LocalDateTime createdDate, int viewCount, int likeCount, Long commentCount,
    String photoUrl, String authorProfilePictureUrl) {
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
        this.photoUrl = photoUrl;
        this.authorProfilePictureUrl = authorProfilePictureUrl;
    }
}