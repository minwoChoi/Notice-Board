package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;   

@Entity
@Getter
@Setter
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "like_count")
    private int likeCount;

    @Lob
    @Column(name = "photo", columnDefinition = "bytea")
    @JdbcTypeCode(SqlTypes.VARBINARY) // 👈 여기도 똑같이 추가하세요.
    private byte[] photo;

    @Column(name = "view_count")
    private int viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "report_count")
    private int reportCount = 0;

    @Column(name = "is_blocked")
    private boolean isBlocked = false;

    //게시물 좋아요 연관매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostLike> postLikes;

    //댓글 연관매핑
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    //스크랩 연관 매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Scrap> scraps = new ArrayList<>();

    //알림 연관매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    // 신고 연관매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Report> reports;    

    // 연관관계 편의 메소드
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }
    // 댓글 삭제
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    // 좋아요
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 취소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    //조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }

    //신고 수 증가
    public void increaseReportCount() {
        this.reportCount++;
    }
}


