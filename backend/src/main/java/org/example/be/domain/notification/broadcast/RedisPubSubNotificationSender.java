package org.example.be.domain.notification.broadcast;

import org.example.be.domain.notification.dto.response.NotificationResBody;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPubSubNotificationSender implements NotificationSender {

	public static final String CHANNEL = "notification-events";

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void send(Long receiverId, NotificationResBody payload) {
		try {
			String json = objectMapper.writeValueAsString(new NotificationBroadcastMessage(receiverId, payload));
			stringRedisTemplate.convertAndSend(CHANNEL, json);
		} catch (Exception e) {
			log.error("[Notification] Redis publish 실패 - receiverId={}", receiverId, e);
		}
	}
}
