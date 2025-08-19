package com.example.demo.service;

import java.beans.Transient;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;

import lombok.AllArgsConstructor;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.model.Comment;

import com.example.demo.dto.comment.response.*;
import com.example.demo.dto.comment.request.*;
@Service
@AllArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transient
    public Comment createComment(Comment comment, String userId, Long postId) {

        // user 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    
        // post 조회 (필요하면)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    
        // 댓글에 user, post 연관관계 세팅
        comment.setUser(user);
        comment.setPost(post);
    
        comment.setLikeCount(0);
        comment.setCreatedDate(LocalDateTime.now());
    
        return commentRepository.save(comment);
    }
    
    @Transactional
    public Comment updateComment(Long postId, Long commentId, CommentEditRequest commentEditRequest, String username) {
        // 댓글 조회
        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
    
        // 댓글이 해당 게시글에 속하는지 반드시 체크 (선택적이지만 권장)
        if (!comment.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified post.");
        }
    
        // 작성자 검증
        if (!comment.getUser().getUserId().equals(username)) {
            throw new SecurityException("이 댓글의 작성자가 아닙니다.");
        }
    
        // 수정 내용 반영
        comment.setContent(commentEditRequest.getContent());
    
        return comment; // JPA 영속성 컨텍스트로 자동 반영
    }
    

}