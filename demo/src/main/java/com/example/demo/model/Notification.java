package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_like_id", referencedColumnName = "post_like_id")
    private PostLike postLike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_like_id", referencedColumnName = "comment_like_id")
    private CommentLike commentLike;

    @Column(name = "message")
    private String message;

    @Column(name = "read")
    private Boolean read;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
