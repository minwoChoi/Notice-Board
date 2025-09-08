package com.example.demo.dto.comment.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor; // 기본 생성자 추가
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 서비스 레이어에서 객체를 생성하고 값을 설정하기 위해 추가합니다.
public class MyCommentResponse {
    // 댓글 정보
    private Long commentId;
    private String content;
    private int likeCount;
    private LocalDateTime createdDate;

    // 댓글이 달린 게시물 정보
    private Long postId;
    private String postTitle;
    
    
    private String profilePictureUrl;

    // JPQL에서 바로 DTO를 생성하기 위한 생성자 (기존 유지)
    public MyCommentResponse(Long commentId, String content, int likeCount, LocalDateTime createdDate, Long postId, String postTitle) {
        this.commentId = commentId;
        this.content = content;
        this.likeCount = likeCount;
        this.createdDate = createdDate;
        this.postId = postId;
        this.postTitle = postTitle;
    }
}