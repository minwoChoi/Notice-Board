package com.example.demo.dto.report.response;

import com.example.demo.model.Report;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReportResponseDto {
    private Long reportId;
    private Long reportedPostId; 
    private String postTitle;   
    private Short reportReason;
    private String reportText;
    private LocalDateTime createdDate;

    public ReportResponseDto(Report report) {
        this.reportId = report.getReportId();
        this.reportedPostId = report.getPost().getPostId();
        this.postTitle = report.getPost().getTitle(); 
        this.reportReason = report.getReportReason();
        this.reportText = report.getReportText();
        this.createdDate = report.getCreatedDate();
    }
}