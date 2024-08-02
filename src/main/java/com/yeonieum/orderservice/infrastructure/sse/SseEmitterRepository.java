package com.yeonieum.orderservice.infrastructure.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 주어진 아이디와 이미터를 저장[고객은 여러개의 이미터를 생성할 수 있음.- 시연을 위해 제한하지 않음.]
     *
     * @param customerId
     * @param emitter
     */
    public void save(Long customerId, SseEmitter emitter) {
        emitters.computeIfAbsent(customerId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    /**
     * 주어진 아이디의 Emitter를 제거
     *
     * @param customerId - 사용자 아이디.
     */
    public void deleteById(Long customerId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(customerId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(customerId);
            }
        }
    }

    /**
     * 주어진 아이디의 Emitter를 가져옴.
     *
     * @param customerId - 사용자 아이디.
     * @return SseEmitter - 이벤트 Emitter.
     */
    public List<SseEmitter> get(Long customerId) {
        return emitters.getOrDefault(customerId, List.of());
    }
}
