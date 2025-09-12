package com.example.demo.dto.notification.response;

import com.example.demo.model.Notification;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponse {
    private Long notificationId;
    private String message;
    private Long postId;
    private Long commentId;
    private Boolean read; // Boolean 타입으로 변경
    private LocalDateTime createdDate;

    public NotificationResponse(Notification notification) {
        this.notificationId = notification.getNotificationId();
        this.message = notification.getMessage();
        this.read = notification.getRead();
        this.createdDate = notification.getCreatedDate();

        if (notification.getPost() != null) {
            this.postId = notification.getPost().getPostId();
        }
        if (notification.getComment() != null) {
            this.commentId = notification.getComment().getCommentId();
        }
    }
}