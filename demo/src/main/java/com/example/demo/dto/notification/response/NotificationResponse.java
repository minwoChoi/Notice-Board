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
    private Boolean read;
    private LocalDateTime createdDate;

    public NotificationResponse(Notification notification) {
        this.notificationId = notification.getNotificationId();
        this.message = notification.getMessage(); // User 모델의 meeeage 필드명을 message로 수정했다고 가정
        this.read = notification.getRead();
        this.createdDate = notification.getCreatedDate();
    }
}
