

package com.example.demo.service;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.repository.EmitterRepository;
import com.example.demo.repository.NotificationRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {
    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;

    private static final long DEFAULT_TIMEOUT = 15L * 60 * 1000; //수명

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT); 
        emitterRepository.add(userId, emitter);   // 유저별 다중 연결 저장
        log.info("SSE emitter created and added for user: {}", userId);
        emitter.onCompletion(() -> {
            emitterRepository.remove(userId, emitter);
        });

        emitter.onError(e -> {
            emitterRepository.remove(userId, emitter);
            try {
                emitter.complete();
            } catch (Exception ex) {
                log.warn("[SSE] complete() after error: userId={}, emitter={}, ex={}",
                        userId, emitter.hashCode(), ex.toString());
            }
        });

        // 초기 이벤트(INIT)
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .id(userId + "_" + System.currentTimeMillis())
                    .data("ok"));
        } catch (IOException e) {
            emitterRepository.remove(userId, emitter);
            emitter.completeWithError(e);
        }
        // 연결 확인을 위한 Ping
        try {
            emitter.send(SseEmitter.event().name("PING").data("ping"));
        } catch (IOException ignore) {
            emitterRepository.remove(userId, emitter);
            emitter.complete();
        }

        return emitter;
    }

    private void sendToClient(String userId, Object data) {
        Collection<SseEmitter> emitters = emitterRepository.getAll(userId);
        if (emitters == null || emitters.isEmpty()) { // emitter가 없다는 건 서버에 살아있는 SSE 객체가 없다는 뜻
            return;
        }

        String eventId = userId + "_" + System.currentTimeMillis();
        log.debug("[SSE] send start: userId={}, eventId={}, targets={}", userId, eventId, emitters.size());

        // 복수 연결 (여러 탭과 같은)
        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            int emitterCode = System.identityHashCode(emitter); // 로그용
            try {
                emitter.send(SseEmitter.event()
                        .name("notice")  // 클라이언트에서 이벤트 이름으로 구독
                        .id(eventId)
                        .data(data));     
                log.trace("[SSE] sent to emitter={}", emitterCode);
            } catch (IOException ex) {
                log.warn("[SSE] send failed: userId={}, eventId={}, emitter={}, ex={}: {}",
                        userId, eventId, emitterCode, ex.getClass().getSimpleName(), ex.getMessage());
                emitterRepository.remove(userId, emitter);
                // emitter 닫기 
                try {
                    emitter.complete();
                } catch (Exception closeEx) {
                    log.debug("[SSE] complete() after send fail threw: emitter={}, ex={}",
                            emitterCode, closeEx.toString());
                }
            }
        }
    }

}

//https://sy-hj08.tistory.com/49