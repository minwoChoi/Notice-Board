package com.example.demo.controller;

import com.example.demo.dto.comment.request.CommentCreateRequest;
import com.example.demo.dto.comment.request.CommentEditRequest;
import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.model.Comment;
import com.example.demo.service.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class CommentController { // [수정] API 경로 일관성을 위해 RequestMapping 제거

    private final CommentService commentService;

    // 특정 게시글의 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId, Authentication authentication) {
        String currentUserId = (authentication != null) ? authentication.getName() : null;
        List<CommentResponse> commentList = commentService.getCommentsByPostId(postId, currentUserId);
        return ResponseEntity.ok(commentList);
    }

    // 특정 사용자가 작성한 댓글 목록 조회
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable String userId, Authentication authentication) {
        // [수정] 현재 로그인한 사용자 정보도 전달하여 '내 댓글' 여부 확인
        String currentUserId = (authentication != null) ? authentication.getName() : null;
        List<CommentResponse> commentList = commentService.getCommentsByUserId(userId, currentUserId);
        return ResponseEntity.ok(commentList);
    }
    
    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest commentCreateRequest,
            Authentication authentication) {

        String username = authentication.getName();
        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        Comment savedComment = commentService.createComment(comment, username, postId);

        // [수정] isAuthor는 true, isLiked는 false로 값을 직접 전달
        CommentResponse response = new CommentResponse(savedComment, true, false);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

   // 댓글 수정
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest commentEditRequest,
            Authentication authentication) {

        String username = authentication.getName();
        // [수정] 서비스 메서드에서 더 이상 postId가 필요 없으므로 제거
        Comment updatedComment = commentService.updateComment(null, commentId, commentEditRequest, username);

        // [수정] 수정된 댓글의 isLiked 상태는 알 수 없으므로, 우선 false로 설정 (필요시 서비스에서 조회)
        CommentResponse response = new CommentResponse(updatedComment, true, false);

        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        String username = authentication.getName();
        commentService.deleteComment(commentId, username);
        return ResponseEntity.noContent().build();
    }

    // 댓글 좋아요 토글
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<Boolean> toggleLikeComment(
            @PathVariable Long commentId,
            Authentication authentication) {
    
        String username = authentication.getName();
        // [수정] 서비스 메서드에서 더 이상 postId가 필요 없으므로 제거
        boolean isLikedNow = commentService.toggleLikeComment(commentId, null, username);
        return ResponseEntity.ok(isLikedNow);
    }
}