package com.example.demo.controller;

import com.example.demo.dto.comment.request.CommentCreateRequest;
import com.example.demo.dto.comment.request.CommentEditRequest;
import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.model.Comment;
import com.example.demo.service.CommentService;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.Fetch;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // [유지] 특정 게시글의 댓글 목록 조회 (기존과 동일)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId, Authentication authentication) {
        String currentUserId = (authentication != null) ? authentication.getName() : null;
        List<CommentResponse> commentList = commentService.getCommentsByPostId(postId, currentUserId);
        return ResponseEntity.ok(commentList);
    }

    // [유지] 특정 사용자가 작성한 댓글 목록 조회 (요청 이미지에 없으나 유용한 기능이므로 유지)
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable String userId, Authentication authentication) {
        String currentUserId = (authentication != null) ? authentication.getName() : null;
        List<CommentResponse> commentList = commentService.getCommentsByUserId(userId, currentUserId);
        return ResponseEntity.ok(commentList);
    }
    
    // [유지] 댓글 작성 (기존과 동일)
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest commentCreateRequest,
            Authentication authentication) {

        String username = authentication.getName();
        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        Comment savedComment = commentService.createComment(comment, username, postId);
        
        CommentResponse response = new CommentResponse(savedComment, true, false);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

   // [변경] 댓글 수정: 경로에 postId 추가 및 PUT 메서드로 변경
    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId, // postId 경로 변수 추가
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest commentEditRequest,
            Authentication authentication) {

        String username = authentication.getName();
        // 서비스 메서드에 postId 전달
        Comment updatedComment = commentService.updateComment(postId, commentId, commentEditRequest, username);
        
        CommentResponse response = new CommentResponse(updatedComment, true, false);

        return ResponseEntity.ok(response);
    }

    // [변경] 댓글 삭제: 경로에 postId 추가
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId, // postId 경로 변수 추가 (서비스 로직에서 검증용으로 활용 가능)
            @PathVariable Long commentId,
            Authentication authentication) {
                
        String username = authentication.getName();
        // postId는 현재 서비스 메서드에서 사용하지 않지만, 일관성을 위해 전달 가능
        commentService.deleteComment(commentId, username);
        return ResponseEntity.noContent().build();
    }

    // [변경] 댓글 좋아요 토글: 경로에 postId 추가
    @PostMapping("/posts/{postId}/comments/{commentId}/likes")
    public ResponseEntity<Boolean> toggleLikeComment(
            @PathVariable Long postId, // postId 경로 변수 추가
            @PathVariable Long commentId,
            Authentication authentication) {
    
        String username = authentication.getName();
        // 서비스 메서드에 postId 전달
        boolean isLikedNow = commentService.toggleLikeComment(commentId, postId, username);
        return ResponseEntity.ok(isLikedNow);
    }
}