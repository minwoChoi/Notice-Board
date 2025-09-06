package com.example.demo.dto.comment.response;

import com.example.demo.model.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private String nickname;
    private String profilePictureUrl;
    private String content;
    private int likeCount;
    private LocalDateTime createdDate;

    private boolean isAuthor;
    private boolean isLiked;

    // 모든 정보를 한 번에 받는 생성자
    public CommentResponse(Comment comment, boolean isAuthor, boolean isLiked) {
        this.commentId = comment.getCommentId();
        this.nickname = comment.getUser().getNickname();
        this.content = comment.getContent();
        this.likeCount = comment.getLikeCount();
        this.createdDate = comment.getCreatedDate();
        this.isAuthor = isAuthor;
        this.isLiked = isLiked;

        if (comment.getUser().getProfilePicture() != null && comment.getUser().getProfilePicture().length > 0) {
            this.profilePictureUrl = "/users/" + comment.getUser().getUserId() + "/photo";
        }
    }
}