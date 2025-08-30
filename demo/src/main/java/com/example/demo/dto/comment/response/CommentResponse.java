package com.example.demo.dto.comment.response;

import com.example.demo.model.Comment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private String nickname;          // 작성자 닉네임
    private String profilePictureUrl; 
    private String content;
    private int likeCount;            // 추천수
    private String createdDate;

    
    public CommentResponse(Comment comment) {
        this.commentId = comment.getCommentId();
        this.nickname = comment.getUser().getNickname();
        this.content = comment.getContent();
        this.likeCount = comment.getLikeCount();
        this.createdDate = comment.getCreatedDate().toString();

        // 👇 사용자 프로필 사진이 있으면 URL을 생성하고, 없으면 null로 설정합니다.
        if (comment.getUser().getProfilePicture() != null && comment.getUser().getProfilePicture().length > 0) {
            this.profilePictureUrl = "/users/" + comment.getUser().getUserId() + "/photo";
        }
    }
}

