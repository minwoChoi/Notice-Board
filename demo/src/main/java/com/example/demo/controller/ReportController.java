package com.example.demo.controller;
import com.example.demo.dto.report.request.ReportRequestDto;
import com.example.demo.dto.report.response.ReportResponseDto;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    // 1. 게시물 신고
    @PostMapping("/posts/{postId}")
    public ResponseEntity<Void> reportPost(
            @PathVariable Long postId,
            @RequestBody ReportRequestDto requestDto,
            Authentication authentication) {
        
        String userId = authentication.getName();
        reportService.createReport(postId, requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 2. 내 신고 내역 조회
    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getMyReports(Authentication authentication) {
        String userId = authentication.getName();
        List<ReportResponseDto> myReports = reportService.getMyReports(userId);
        return ResponseEntity.ok(myReports);
    }

    // 3. 특정 신고 내역 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDto> getReportDetails(
            @PathVariable("id") Long reportId,
            Authentication authentication) {
                
        String userId = authentication.getName();
        ReportResponseDto reportDetails = reportService.getReportDetails(reportId, userId);
        return ResponseEntity.ok(reportDetails);
    }
}