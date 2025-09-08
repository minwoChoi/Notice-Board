package com.example.demo.dto.report.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDto {
    private Short reportReason; // 신고 사유 코드 (예: 1: 스팸, 2: 욕설)
    private String reportText;   // 상세 내용
}