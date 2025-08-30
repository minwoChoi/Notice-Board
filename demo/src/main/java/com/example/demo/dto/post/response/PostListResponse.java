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
    private String categoryName;
    private String title;
    private String content;
    private String photoUrl; // 게시물 자체의 사진 URL
    private String nickname;
    private LocalDateTime createdDate;
    private int likeCount;
    private int viewCount;
    private Long commentCount;

    private String authorProfilePictureUrl;

    // JPQL 결과를 직접 매핑하기 위한 생성자 수정 (기존 유지)
    public PostListResponse(Long postId, String categoryName, String title, String content, byte[] photo, 
                            String nickname, LocalDateTime createdDate, int likeCount, int viewCount, Long commentCount) {
        this.postId = postId;
        this.categoryName = categoryName;
        this.title = title;
        this.content = content;
        this.nickname = nickname;
        this.createdDate = createdDate;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.commentCount = commentCount;

        if (photo != null && photo.length > 0) {
            this.photoUrl = "/posts/" + postId + "/photo";
        }
    }
}