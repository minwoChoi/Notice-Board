package com.example.demo.service;

import com.example.demo.dto.report.request.ReportRequestDto;
import com.example.demo.dto.report.response.ReportResponseDto;
import com.example.demo.model.Post;
import com.example.demo.model.Report;
import com.example.demo.model.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 1. 게시물 신고하기
    @Transactional
    public void createReport(Long postId, ReportRequestDto requestDto, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 중복 신고 방지
        if (reportRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("이미 신고한 게시물입니다.");
        }

        Report report = new Report();
        report.setUser(user);
        report.setPost(post);
        report.setReportReason(requestDto.getReportReason());
        report.setReportText(requestDto.getReportText());
        report.setCreatedDate(LocalDateTime.now());

        reportRepository.save(report);
    }

    // 2. 내 신고 내역 목록 조회
    public List<ReportResponseDto> getMyReports(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        List<Report> reports = reportRepository.findByUserOrderByCreatedDateDesc(user);
        return reports.stream()
                .map(ReportResponseDto::new)
                .collect(Collectors.toList());
    }

    // 3. 신고 내역 상세 조회
    public ReportResponseDto getReportDetails(Long reportId, String userId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        // 본인의 신고 내역이 맞는지 확인 (보안)
        if (!report.getUser().getUserId().equals(userId)) {
            throw new SecurityException("조회 권한이 없습니다.");
        }

        return new ReportResponseDto(report);
    }
}