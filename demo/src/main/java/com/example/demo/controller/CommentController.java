package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("posts")
public class CommentController {
    // 댓글 목록 조회 (특정 게시글의 댓글들)
    @GetMapping("/{postId}/comments")
    public String getCommentList(@PathVariable Long postId) {
        return "";
    }

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public String createComment(@PathVariable Long postId, @RequestBody String entity) {
        return entity;
    }

    // 댓글 수정
    @PutMapping("/{id}/comments/{commentId}")
    public String updateComment(@PathVariable Long commentId, @RequestBody String entity) {
        return entity;
    }

    // 댓글 삭제
    @DeleteMapping("/{id}/comments/{Id}")
    public String deleteComment(@PathVariable Long commentId) {
        return "";
    }

    // 댓글 추천
    @PostMapping("{postId}/comments/{commentId}/likes")
    public String likeComment(@PathVariable Long commentId) {
        return "";
    }

    // 댓글 추천 취소
    @DeleteMapping("{postId}/comments/{commentId}/likes")
    public String unlikeComment(@PathVariable Long commentId) {
        return "";
    }

}
