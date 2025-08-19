package com.example.demo.dto.comment.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponse {
    private Long commentId;
    private String nickname;          // 작성자 닉네임
    private String profilePicture;  //작성자 사진
    private String content;
    private int likeCount;            // 추천수
    private String createdDate;
}

