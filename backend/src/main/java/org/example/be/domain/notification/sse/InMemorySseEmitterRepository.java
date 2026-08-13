package org.example.be.domain.notification.sse;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class InMemorySseEmitterRepository implements SseEmitterRepository {

	private final Map<Long, Map<String, SseEmitter>> store = new ConcurrentHashMap<>();

	@Override
	public SseEmitter save(Long memberId, String emitterId, SseEmitter emitter) {
		store.computeIfAbsent(memberId, id -> new ConcurrentHashMap<>()).put(emitterId, emitter);
		return emitter;
	}

	@Override
	public Map<String, SseEmitter> findAllByMemberId(Long memberId) {
		return store.getOrDefault(memberId, Collections.emptyMap());
	}

	@Override
	public void delete(Long memberId, String emitterId) {
		Map<String, SseEmitter> emitters = store.get(memberId);
		if (emitters == null) {
			return;
		}
		emitters.remove(emitterId);
		if (emitters.isEmpty()) {
			store.remove(memberId, emitters);
		}
	}

	@Override
	public void deleteAllByMemberId(Long memberId) {
		store.remove(memberId);
	}

	@Override
	public Map<Long, Map<String, SseEmitter>> findAll() {
		return store;
	}
}
