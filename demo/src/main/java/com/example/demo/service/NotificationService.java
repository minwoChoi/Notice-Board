package com.example.demo.service;

import com.example.demo.dto.notification.response.NotificationResponse; // DTO 패키지 경로에 맞게 수정해주세요
import com.example.demo.model.Notification;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.model.Comment;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 모든 알림 목록을 조회합니다.
     * 
     * @param user 현재 로그인한 사용자
     * @return 알림 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        List<Notification> notifications = notificationRepository
                .findAllByUser_UserIdOrderByCreatedDateDesc(user.getUserId());
        return notifications.stream().map(NotificationResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return notificationRepository.countByUser_UserIdAndReadIsFalse(user.getUserId());
    }

    /**
     * 특정 알림을 읽음 상태로 변경합니다.
     * 
     * @param notificationId 읽음 처리할 알림의 ID
     * @param user           현재 로그인한 사용자
     */

    @Transactional
    public void markAsRead(Long notificationId, String userId) throws AccessDeniedException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 알림을 찾을 수 없습니다: " + notificationId));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해該 알림에 접근할 권한이 없습니다.");
        }

        notification.setRead(true);
    }

    /**
     * 사용자의 읽지 않은 알림 개수를 조회합니다.
     * 
     * @param user 현재 로그인한 사용자
     * @return 읽지 않은 알림 개수
     */

    // 알림 발생 시점
    @Transactional
    public void createNotification(User userTo, String message, Post post, Comment comment) {
        Notification notification = new Notification();
        notification.setUser(userTo);
        notification.setMessage(message);
        notification.setPost(post);
        notification.setComment(comment);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());

        notificationRepository.save(notification);

        // 알림 생성 후 실시간 SSE 전송
        NotificationResponse response = new NotificationResponse(notification);
        // NotificationController의 sendEventToUser 호출 필요 (또는 이벤트 발행 통해 연결)
        // 예: notificationController.sendEventToUser(userTo.getUserId(), response);
    }
}