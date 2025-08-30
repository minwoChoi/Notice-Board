package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.comment.request.CommentEditRequest;
import com.example.demo.dto.comment.response.CommentResponse;
import com.example.demo.dto.comment.response.MyCommentResponse;
import com.example.demo.model.Comment;
import com.example.demo.model.CommentLike;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.CommentLikeRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationService notificationService;


    @Transactional(readOnly = true)
    public List<MyCommentResponse> findMyComments(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 1. 기존 로직으로 DTO 리스트를 먼저 조회합니다.
        // 이 시점의 DTO에는 아직 profilePictureUrl이 없습니다.
        List<MyCommentResponse> responseList = commentRepository.findCommentsByUserWithPostInfo(user);
    
        // 2. 사용자의 프로필 사진 URL을 생성합니다. (사진이 없으면 null)
        String profilePictureUrl = (user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                ? "/users/" + userId + "/photo"
                : null;

        // 3. 조회된 각 DTO에 프로필 사진 URL을 설정해줍니다.
        responseList.forEach(dto -> dto.setProfilePictureUrl(profilePictureUrl));

        // 4. URL이 추가된 리스트를 반환합니다.
        return responseList;
    }
    //댓글 작성
    // [수정] @Transient -> @Transactional로 변경
    // com/example/demo/service/CommentService.java

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

        // [수정 1] 댓글을 먼저 저장하여 영속화하고 ID를 부여받습니다.
        Comment savedComment = commentRepository.save(comment);

        // [수정 2] 자기 자신에게는 알림을 보내지 않도록 처리합니다.
        if (!post.getUser().equals(user)) {
            String message = user.getNickname() + "님이 회원님의 게시물에 댓글을 남겼습니다.";
            // [수정 3] 저장된 댓글(savedComment)을 알림 생성에 사용합니다.
            notificationService.createNotification(post.getUser(), message, post, savedComment);
        }
        
        return savedComment;
    }
    
    //댓글 수정
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
    
    //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
    
        if (!comment.getUser().getUserId().equals(username)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
    
        commentRepository.delete(comment);
    }
    
    //댓글 추천
    @Transactional
    public void likeComment(Long commentId, Long postId, String userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
            throw new IllegalStateException("이미 추천한 댓글입니다.");
        }

        CommentLike like = new CommentLike();
        like.setComment(comment);
        like.setUser(user);
        commentLikeRepository.save(like);
        
        comment.increaseLikeCount();
        // [개선] 이 save 호출은 사실상 불필요하지만, 명시적으로 둘 수도 있습니다.

        // [추가] 댓글 작성자에게 알림 보내기
        String message = user.getNickname() + "님이 회원님의 댓글을 추천했습니다.";
        notificationService.createNotification(comment.getUser(), message, comment.getPost(), comment);
    }

    //댓글 추천 취소
    @Transactional
    public void unlikeComment(Long commentId, Long postId, String userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        CommentLike commentLike = commentLikeRepository.findByCommentAndUser(comment, user)
            .orElseThrow(() -> new IllegalArgumentException("추천하지 않은 댓글입니다."));
        
        commentLikeRepository.delete(commentLike);
        comment.decreaseLikeCount();
    }

    //게시물 댓글 목록 조회
    // [개선] 읽기 전용 트랜잭션 추가
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPost_PostId(postId);
        return comments.stream().map(CommentResponse::new).collect(Collectors.toList());
    }

    // [개선] 읽기 전용 트랜잭션 추가
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUserId(String userId) {
        List<Comment> comments = commentRepository.findByUser_UserId(userId);
        return comments.stream().map(CommentResponse::new).collect(Collectors.toList());
    }
}