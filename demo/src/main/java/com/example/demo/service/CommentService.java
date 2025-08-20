package com.example.demo.service;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

import com.example.demo.repository.CommentLikeRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import lombok.AllArgsConstructor;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.model.Comment;
import com.example.demo.model.CommentLike;

import com.example.demo.dto.comment.request.*;
import com.example.demo.dto.comment.response.CommentResponse;
@Service
@AllArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;

    //댓글 작성
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
    
    //댓글 수정
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
    
    //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        // 1. 댓글 조회
        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
    
        // 2. 작성자 검증: 작성자 본인인지 확인
        if (!comment.getUser().getUserId().equals(username)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
    
        // 3. 삭제 처리
        commentRepository.delete(comment);
    }
    
    //댓글 추천
    @Transactional
    public void likeComment(Long commentId, Long postId, String userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        boolean alreadyLiked = commentLikeRepository.existsByCommentAndUser(comment, user);
        if (alreadyLiked) {
            throw new IllegalStateException("이미 추천한 댓글입니다.");
        }

        CommentLike like = new CommentLike();
        like.setComment(comment);
        like.setUser(user);
        commentLikeRepository.save(like);
        
        comment.increaseLikeCount();
        commentRepository.save(comment);
    }

    //댓글 추천 취소
    @Transactional
    public void unlikeComment(Long commentId, Long postId, String userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        CommentLike commentLike = commentLikeRepository.findByCommentAndUser(comment, user)
            .orElseThrow(() -> new IllegalArgumentException("추천하지 않은 게시글입니다."));
        
        commentLikeRepository.delete(commentLike);

        comment.decreaseLikeCount();
        commentRepository.save(comment);

        
    }

    //게시물 댓글 목록 조회
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPost_PostId(postId);

        return comments.stream()
            .map(comment -> {
                CommentResponse dto = new CommentResponse();
                dto.setCommentId(comment.getCommentId());
                dto.setNickname(comment.getUser().getNickname());
                dto.setContent(comment.getContent());
                dto.setProfilePicture(comment.getUser().getProfilePicture());
                dto.setLikeCount(comment.getLikeCount());
                dto.setCreatedDate(comment.getCreatedDate().toString());  // 필요하면 포맷터 사용
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<CommentResponse> getCommentsByUserId(String userId) {
        List<Comment> comments = commentRepository.findByUser_UserId(userId);
        return comments.stream()
            .map(comment -> {
                CommentResponse dto = new CommentResponse();
                dto.setCommentId(comment.getCommentId());
                dto.setNickname(comment.getUser().getNickname());
                dto.setContent(comment.getContent());
                dto.setProfilePicture(comment.getUser().getProfilePicture());
                dto.setLikeCount(comment.getLikeCount());
                dto.setCreatedDate(comment.getCreatedDate().toString()); // 필요하면 포맷터 사용
                return dto;
            })
            .collect(Collectors.toList());
    }
    

    
}