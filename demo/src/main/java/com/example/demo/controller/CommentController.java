package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.comment.request.CommentCreateRequest;
import com.example.demo.model.Comment;

import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.service.CommentService;
import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.dto.comment.request.CommentEditRequest;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

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

        // 엔티티 → DTO 변환
        CommentResponse response = new CommentResponse();
        response.setCommentId(savedComment.getCommentId());
        response.setNickname(savedComment.getUser().getNickname());
        response.setContent(savedComment.getContent());
        response.setProfilePicture(savedComment.getUser().getProfilePicture());
        response.setLikeCount(savedComment.getLikeCount());
        response.setCreatedDate(savedComment.getCreatedDate().toString()); // or 포맷팅

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

        CommentResponse response = new CommentResponse();
        response.setCommentId(updatedComment.getCommentId());
        response.setNickname(updatedComment.getUser().getNickname());
        response.setContent(updatedComment.getContent());
        response.setProfilePicture(updatedComment.getUser().getProfilePicture());
        response.setLikeCount(updatedComment.getLikeCount());
        response.setCreatedDate(updatedComment.getCreatedDate().toString()); // or 포맷팅

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
