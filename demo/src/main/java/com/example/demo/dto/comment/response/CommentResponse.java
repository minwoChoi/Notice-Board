package com.example.demo.dto.comment.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.demo.model.*;;
@Getter
@Setter
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private String nickname;          // 작성자 닉네임
    private String profilePicture;  //작성자 사진
    private String content;
    private int likeCount;            // 추천수
    private String createdDate;

    public CommentResponse(Comment comment) {
        this.commentId = comment.getCommentId();
        this.nickname = comment.getUser().getNickname();
        this.content = comment.getContent();
        this.profilePicture = comment.getUser().getProfilePicture();
        this.likeCount = comment.getLikeCount();
        this.createdDate = comment.getCreatedDate().toString(); // 필요하다면 날짜 포맷팅
    }
}

