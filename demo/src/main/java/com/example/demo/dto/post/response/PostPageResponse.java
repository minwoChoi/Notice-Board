package com.example.demo.dto.post.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostPageResponse {
    private List<PostListResponse> posts; // 현재 페이지의 게시물 목록
    private long totalPostCount;          // 전체 게시물 수
    // private int totalPages;            // 전체 페이지 수
    // private int currentPage;           // 현재 페이지 번호
}
