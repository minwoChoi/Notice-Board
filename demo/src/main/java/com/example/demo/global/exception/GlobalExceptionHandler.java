// package com.example.demo.global.exception;

// import jakarta.servlet.http.HttpServletRequest;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// import java.util.Map;

// @Slf4j
// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     // 아이디 중복 예외 처리
//     @ExceptionHandler(DuplicateUserIdException.class)
//     public ResponseEntity<Map<String, Object>> handleDuplicateUserId(DuplicateUserIdException ex) {
//         log.warn("[DuplicateUserIdException] {}", ex.getMessage());
//         return ResponseEntity.status(HttpStatus.CONFLICT)
//                 .body(Map.of(
//                         "available", false,
//                         "message", ex.getMessage()
//                 ));
//     }

//     // 닉네임 중복 예외 처리
//     @ExceptionHandler(DuplicateUserNicknameException.class)
//     public ResponseEntity<Map<String, Object>> handleDuplicateUserNickname(DuplicateUserNicknameException ex) {
//         log.warn("[DuplicateUserNicknameException] {}", ex.getMessage());
//         return ResponseEntity.status(HttpStatus.CONFLICT)
//                 .body(Map.of(
//                         "available", false,
//                         "message", ex.getMessage()
//                 ));
//     }

//     // 기타 모든 예외 처리
//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
//         String uri = request.getRequestURI();
//         log.error("[Exception] 요청 경로: {} 예외 발생", uri, ex);

//         // Swagger 문서 요청(/v3/api-docs, /swagger-ui) 예외는 무시하고 빈 JSON 반환
//         if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
//             log.debug("Swagger 요청 예외 무시: {}", uri);
//             return ResponseEntity.ok(Map.of());
//         }

//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body(Map.of(
//                         "available", false,
//                         "message", "서버 오류가 발생했습니다.",
//                         "errorDetail", String.valueOf(ex.getMessage())
//                 ));
//     }
// }
