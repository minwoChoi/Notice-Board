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
    private String categoryName;
    private String title;
    private String content;
    private String photoUrl;
    private String nickname;
    private LocalDateTime createdDate;
    private int likeCount;
    private int viewCount;
    private List<CommentResponse> comments; // 이 부분을 추가합니다
}