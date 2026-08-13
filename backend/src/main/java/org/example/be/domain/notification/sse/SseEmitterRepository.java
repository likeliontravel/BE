package org.example.be.domain.notification.sse;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterRepository {

	SseEmitter save(Long memberId, String emitterId, SseEmitter emitter);

	Map<String, SseEmitter> findAllByMemberId(Long memberId);

	void delete(Long memberId, String emitterId);

	void deleteAllByMemberId(Long memberId);

	Map<Long, Map<String, SseEmitter>> findAll();
}
