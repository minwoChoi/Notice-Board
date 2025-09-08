package com.example.demo.controller;

import com.example.demo.dto.notification.response.NotificationResponse;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.demo.repository.*;
import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    // 알림 목록 조회 API (GET /notifications)
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // 알림 읽음 처리 API (PUT /notifications/{notificationId}/read)
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) throws AccessDeniedException {
        String username = authentication.getName();
        notificationService.markAsRead(notificationId, username);
        return ResponseEntity.ok().build();
    }

    // 읽지 않은 알림 개수 조회 API (GET /notifications/unread-count)
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = Collections.singletonMap("count", count);
        return ResponseEntity.ok(response);
    }
}