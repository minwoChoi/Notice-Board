package com.example.demo.global.util;

// Spring Boot 3.x 이상은 jakarta.servlet을 사용합니다.
import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtil {

    /**
     * HttpServletRequest의 User-Agent 헤더를 분석하여 디바이스 유형을 반환합니다.
     * @param request HTTP 요청 객체
     * @return "mobile", "tablet", "pc" 중 하나
     */
    public static String getDeviceType(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        // User-Agent 헤더가 없는 경우 기본값 "pc" 반환
        if (userAgent == null) {
            return "pc";
        }

        userAgent = userAgent.toLowerCase();
        System.out.println(userAgent);
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet")) {
            return "tablet";
        } else {
            return "pc";
        }
    }
}