package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comment")
public class Comments {
    @Id
    @Column(name = "comment_id")
    private int commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "post_id", referencedColumnName = "post_id")
    private Post postId;

    @Column(name = "comment")
    private String content;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "created_date")
    private LocalDateTime createdDate; 
}
