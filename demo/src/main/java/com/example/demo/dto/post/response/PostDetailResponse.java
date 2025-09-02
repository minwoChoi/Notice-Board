package com.example.demo.dto.post.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.dto.comment.response.CommentResponse;

import lombok.Getter;
import lombok.Setter;;

@Getter
@Setter
public class PostDetailResponse {
    private Long postId;
    private String userId;
    private Long categoryId ; //게시물 사진
    //private String categoryName;
    private String title;
    private String content;
    private String photoUrl; //게시물 사진
    private String nickname;
    private LocalDateTime createdDate;
    private boolean isBlocked;
    private int likeCount;
    private int viewCount;
    private String authorProfilePictureUrl;
    private List<CommentResponse> comments; // 이 부분을 추가합니다
}