package com.example.demo.repository;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 ID로 특정 알림 조회
    Optional<Notification> findByNotificationId(Long notificationId);

    List<Notification> findAllByUser_UserIdOrderByCreatedDateDesc(String userId);
    long countByUser_UserIdAndReadIsFalse(String userId);

}