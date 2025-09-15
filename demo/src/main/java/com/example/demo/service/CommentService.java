package com.example.demo.service;

import com.example.demo.dto.comment.request.CommentEditRequest;
import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.dto.comment.response.MyCommentResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationService notificationService;

    // [유지] 내 댓글 목록 조회 (MyCommentResponse DTO 사용)
    @Transactional(readOnly = true)
    public List<MyCommentResponse> findMyComments(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<MyCommentResponse> responseList = commentRepository.findCommentsByUserWithPostInfo(user);

        String profilePictureUrl = (user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                ? "/users/" + userId + "/photo"
                : null;

        responseList.forEach(dto -> dto.setProfilePictureUrl(profilePictureUrl));
        return responseList;
    }

    // [유지] 댓글 작성 로직
    @Transactional
    public Comment createComment(Comment comment, String userId, Long postId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        comment.setUser(user);
        comment.setPost(post);
        comment.setLikeCount(0);
        comment.setCreatedDate(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        if (!post.getUser().equals(user)) {
            String message = user.getNickname() + "님이 회원님의 게시물에 댓글을 남겼습니다.";
            notificationService.createNotification(post.getUser(), message, post, savedComment);
        }
        return savedComment;
    }

    // [유지] 댓글 수정 로직
    @Transactional
    public Comment updateComment(Long postId, Long commentId, CommentEditRequest commentEditRequest, String username) {
        Comment comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        if (!comment.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified post.");
        }
        if (!comment.getUser().getUserId().equals(username)) {
            throw new SecurityException("이 댓글의 작성자가 아닙니다.");
        }
        comment.setContent(commentEditRequest.getContent());
        return comment;
    }

    // [유지] 댓글 삭제 로직
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        if (!comment.getUser().getUserId().equals(username)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    // [수정] 게시물 댓글 목록 조회 (isAuthor, isLiked 추가)
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId, String currentUserId) {
        List<Comment> comments = commentRepository.findByPost_PostId(postId);

        // 로그인한 경우, '좋아요' 누른 댓글 ID 목록을 한 번에 조회
        Set<Long> likedCommentIds = (currentUserId != null && !comments.isEmpty())
                ? commentLikeRepository.findLikedCommentIdsByUserAndComments(currentUserId, comments)
                : Collections.emptySet();

        return comments.stream().map(comment -> {
            boolean isAuthor = currentUserId != null && currentUserId.equals(comment.getUser().getUserId());
            boolean isLiked = likedCommentIds.contains(comment.getCommentId());
            return new CommentResponse(comment, isAuthor, isLiked);
        }).collect(Collectors.toList());
    }

    // [수정] 특정 사용자가 쓴 댓글 목록 조회 (isAuthor, isLiked 추가)
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUserId(String targetUserId, String currentUserId) {
        List<Comment> comments = commentRepository.findByUser_UserId(targetUserId);
        
        // 이 API에서도 '좋아요' 여부를 확인하고 싶다면 위와 동일한 로직 추가 가능
        // 여기서는 isAuthor 확인 로직만 추가
        return comments.stream().map(comment -> {
            boolean isAuthor = currentUserId != null && currentUserId.equals(comment.getUser().getUserId());
            // isLiked 정보는 이 API에서 중요하지 않다고 가정하고 false로 설정
            return new CommentResponse(comment, isAuthor, false);
        }).collect(Collectors.toList());
    }
    
    // [수정] 댓글 좋아요 토글 (불필요한 save 호출 제거)
    @Transactional
    public boolean toggleLikeComment(Long commentId, Long postId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);
        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.decreaseLikeCount();
            // @Transactional에 의해 자동 저장되므로 save 호출 불필요
            return false;
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            commentLikeRepository.save(like);
            comment.increaseLikeCount();
            // @Transactional에 의해 자동 저장되므로 save 호출 불필요

            if (!user.getUserId().equals(comment.getUser().getUserId())) {

                String message = user.getNickname() + "님이 회원님의 댓글을 추천했습니다.";
                notificationService.createNotification(comment.getUser(), message, comment.getPost(), comment);
            }
            return true;
        }
    }
}