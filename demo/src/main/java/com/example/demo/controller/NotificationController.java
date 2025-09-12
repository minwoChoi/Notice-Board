package com.example.demo.controller;

import com.example.demo.dto.notification.response.NotificationResponse;
import com.example.demo.model.User;
import com.example.demo.service.SseService;
import com.example.demo.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.*;
import com.example.demo.repository.*;
import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;

@Slf4j
@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {

    private final SseService sseService; 
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    
    @GetMapping(value = "/stream" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotfication(Authentication authentication) {
        log.info("SSE subscription request received for user: {}", authentication.getName()); // 💡 로그 추가
        String userId = authentication.getName();

        return sseService.subscribe(userId);
    }

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