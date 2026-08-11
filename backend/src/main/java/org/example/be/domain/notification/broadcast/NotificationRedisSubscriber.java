package org.example.be.domain.notification.broadcast;

import java.io.IOException;
import java.util.Map;

import org.example.be.domain.notification.dto.response.NotificationResBody;
import org.example.be.domain.notification.sse.SseEmitterRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class NotificationRedisSubscriber implements MessageListener {

	private final SseEmitterRepository sseEmitterRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			NotificationBroadcastMessage broadcast = objectMapper.readValue(message.getBody(),
				NotificationBroadcastMessage.class);
			send(broadcast.receiverId(), broadcast.payload());

		} catch (Exception e) {
			log.error("[Notification] Redis 메시지 처리 실패", e);
		}

	}

	private void send(Long receiverId, NotificationResBody payload) {
		Map<String, SseEmitter> emitters = sseEmitterRepository.findAllByMemberId(receiverId);
		if (emitters.isEmpty()) {
			return;
		}
		emitters.forEach((emitterId, emitter) -> {
			try {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(payload.id()))
					.name("notification")
					.data(payload));
			} catch (IOException e) {
				sseEmitterRepository.delete(receiverId, emitterId);
				emitter.completeWithError(e);
			}
		});
	}

}
