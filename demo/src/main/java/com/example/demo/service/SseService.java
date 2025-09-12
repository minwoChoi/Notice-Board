package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.repository.EmitterRepository;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor // Lombok이 final 필드를 포함한 생성자를 자동으로 만들어줍니다.
@Slf4j
public class SseService {

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    // ✅ Spring이 관리하는 ObjectMapper Bean을 주입받도록 변경합니다.
    private final ObjectMapper objectMapper; 

    private static final long DEFAULT_TIMEOUT = 15L * 60 * 1000;

    // ❌ private final ObjectMapper objectMapper = new ObjectMapper(); // 이 줄은 완전히 삭제합니다.

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT); 
        emitterRepository.add(userId, emitter);
        log.info("SSE emitter created and added for user: {}", userId);

        emitter.onCompletion(() -> emitterRepository.remove(userId, emitter));

        emitter.onError(e -> {
            emitterRepository.remove(userId, emitter);
            try { emitter.complete(); } 
            catch (Exception ex) { 
                log.warn("[SSE] complete() after error: userId={}, emitter={}, ex={}", userId, emitter.hashCode(), ex.toString());
            }
        });

        // 초기 이벤트(INIT)
        try {
            emitter.send(SseEmitter.event().name("INIT").id(userId + "_" + System.currentTimeMillis()).data("ok"));
        } catch (IOException e) {
            emitterRepository.remove(userId, emitter);
            emitter.completeWithError(e);
        }
        // 연결 확인용 Ping
        try {
            emitter.send(SseEmitter.event().name("PING").data("ping"));
        } catch (IOException ignore) {
            emitterRepository.remove(userId, emitter);
            emitter.complete();
        }

        return emitter;
    }

    public void sendToClient(String userId, Object data) {
        Collection<SseEmitter> emitters = emitterRepository.getAll(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("[SSE] No emitters found for userId={}", userId);
            return;
        }

        String eventId = userId + "_" + System.currentTimeMillis();
        String jsonData;
        try {
            // 이제 이 objectMapper는 LocalDateTime을 처리할 줄 아는 똑똑한 객체입니다.
            jsonData = objectMapper.writeValueAsString(data);
            log.info("[SSE] JSON to send: {}", jsonData);
        } catch (Exception e) {
            log.error("[SSE] Serialization error", e);
            return;
        }

        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            int emitterCode = System.identityHashCode(emitter);
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .id(eventId)
                        .data(jsonData)); 
                log.info("[SSE] Notification sent successfully: userId={}, emitter={}", userId, emitterCode);
            } catch (IOException ex) {
                log.warn("[SSE] send failed: userId={}, eventId={}, emitter={}, ex={}: {}",
                        userId, eventId, emitterCode, ex.getClass().getSimpleName(), ex.getMessage());
                emitterRepository.remove(userId, emitter);
                try { emitter.complete(); } catch (Exception closeEx) { }
            }
        }
    }
}