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
    private Long categoryId ; //게시물 사진
    private String title;
    private String content;
    private String photoUrl; //게시물 사진
    private String nickname;
    private LocalDateTime createdDate;
    private boolean isBlocked;
    private int likeCount;
    private int viewCount;
    private String authorProfilePictureUrl;
    private boolean isAuthor;
    private boolean isLiked;
    private boolean isScrapped; 
    private List<CommentResponse> comments;
}