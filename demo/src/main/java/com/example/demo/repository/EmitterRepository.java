package com.example.demo.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.*;

@Repository
public class EmitterRepository {
    // 유저 ID (String) → 연결된 모든 SSE emitter
    private final Map<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * emitter 추가
     */
    public void add(String userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(emitter);
    }

    /**
     * 특정 emitter 제거
     */
    public void remove(String userId, SseEmitter emitter) {
        Set<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId); // 더 이상 연결 없으면 ConcurrentHashMap에서도 제거
            }
        }
    }

    /**
     * 유저 ID (String)로 모든 emitter 조회
     */
    public Set<SseEmitter> getAll(String userId) {
        return emitters.getOrDefault(userId, Collections.emptySet());
    }
}