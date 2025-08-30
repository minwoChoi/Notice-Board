package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.comment.request.CommentCreateRequest;
import com.example.demo.dto.comment.request.CommentEditRequest;
import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.model.Comment;
import com.example.demo.service.CommentService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/posts")
public class CommentController {

    private final CommentService commentService;

    // 댓글 목록 조회 (특정 게시글의 댓글들)
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getAllCommentList(@PathVariable Long postId, Authentication authentication) {
        List<CommentResponse> commentList = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(commentList);
    }

    //특정 Id의 댓글 목록 조회
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(
        @PathVariable String userId) {
        List<CommentResponse> commentList = commentService.getCommentsByUserId(userId);
        return ResponseEntity.ok(commentList);
    }
    
    // 댓글 작성
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable("id") Long postId,
            @RequestBody CommentCreateRequest commentCreateRequest,
            Authentication authentication) {

        String username = authentication.getName();
        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());

        Comment savedComment = commentService.createComment(comment, username, postId);

        // 👇 엔티티를 DTO로 변환하는 로직이 생성자 호출 한 줄로 깔끔해집니다.
        CommentResponse response = new CommentResponse(savedComment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

   // 댓글 수정
    @PatchMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest commentEditRequest,
            Authentication authentication) {

        String username = authentication.getName();
        Comment updatedComment = commentService.updateComment(postId, commentId, commentEditRequest, username);

        // 👇 여기도 생성자 호출 한 줄로 변경합니다.
        CommentResponse response = new CommentResponse(updatedComment);

        return ResponseEntity.ok(response);
    }

    //댓글 삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        String username = authentication.getName();
        commentService.deleteComment(commentId, username);
        return ResponseEntity.noContent().build();
    }

    //댓글 추천
    @PostMapping("/{postId}/comments/{commentId}/likes")
    public ResponseEntity<Void> likeComment(
        @PathVariable  Long commentId,
        @PathVariable  Long postId,
        Authentication authentication) {

        String username = authentication.getName();
        commentService.likeComment(commentId, postId, username);
    
        return ResponseEntity.ok().build();
    }
    
    //댓글 추천 삭제
    @DeleteMapping("/{postId}/comments/{commentId}/likes")
    public ResponseEntity<Void> unlikeComment(
        @PathVariable  Long commentId,
        @PathVariable  Long postId,
        Authentication authentication) {
        String username = authentication.getName();
        commentService.unlikeComment(commentId, postId, username);
        return ResponseEntity.ok().build();
    }

}
