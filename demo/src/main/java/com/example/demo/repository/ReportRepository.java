package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.Report;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 유저의 모든 신고 내역을 최신순으로 조회
    List<Report> findByUserOrderByCreatedDateDesc(User user);

    // 특정 유저가 특정 게시물을 신고했는지 확인
    boolean existsByUserAndPost(User user, Post post);
}