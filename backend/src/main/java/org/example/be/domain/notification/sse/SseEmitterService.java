package org.example.be.domain.notification.sse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.example.be.domain.notification.dto.response.NotificationResBody;
import org.example.be.domain.notification.entity.Notification;
import org.example.be.domain.notification.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterService {

	private static final long TIMEOUT = 60L * 60 * 1000; // 60분

	private final SseEmitterRepository sseEmitterRepository;
	private final NotificationRepository notificationRepository;

	public SseEmitter subscribe(Long memberId, String lastEventId) {
		String emitterId = memberId + "_" + UUID.randomUUID();
		SseEmitter emitter = new SseEmitter(TIMEOUT);
		sseEmitterRepository.save(memberId, emitterId, emitter);

		emitter.onCompletion(() -> sseEmitterRepository.delete(memberId, emitterId));
		emitter.onTimeout(() -> {
			sseEmitterRepository.delete(memberId, emitterId);
			emitter.complete();
		});
		emitter.onError(e -> {
			sseEmitterRepository.delete(memberId, emitterId);
			emitter.complete();
		});

		if (!sendConnectEvent(memberId, emitterId, emitter)) {
			return emitter;
		}
		sendMissedNotifications(memberId, emitterId, lastEventId, emitter);

		return emitter;
	}

	@Scheduled(fixedRate = 30_000)
	public void sendHeartbeat() {
		sseEmitterRepository.findAll().forEach((memberId, emitters) ->
			emitters.forEach((emitterId, emitter) -> {
				try {
					emitter.send(SseEmitter.event().comment("keep-alive"));
				} catch (IOException e) {
					sseEmitterRepository.delete(memberId, emitterId);
					emitter.completeWithError(e);
				}
			})
		);
	}

	// 연결 직후 더미 이벤트 - 첫 바이트가 안 나가면 AsyncRequestTimeoutException으로 이어짐
	private boolean sendConnectEvent(Long memberId, String emitterId, SseEmitter emitter) {
		try {
			long unreadCount = notificationRepository.countByReceiverIdAndReadFalse(memberId);
			emitter.send(SseEmitter.event()
				.name("connect")
				.data(new ConnectPayload("알림 스트림에 연결되었습니다.", unreadCount)));
			return true;
		} catch (IOException e) {
			sseEmitterRepository.delete(memberId, emitterId);
			emitter.completeWithError(e);
			return false;
		}
	}

	// Last-Event-ID 갭 복구 - 파싱 실패 시 조용히 무시하고 빈 스트림으로 시작
	private void sendMissedNotifications(Long memberId, String emitterId, String lastEventId, SseEmitter emitter) {
		if (lastEventId == null) {
			return;
		}

		Long lastId;
		try {
			lastId = Long.parseLong(lastEventId);
		} catch (NumberFormatException e) {
			return;
		}

		List<Notification> missed =
			notificationRepository.findTop50ByReceiverIdAndIdGreaterThanOrderByIdAsc(memberId, lastId);

		for (Notification notification : missed) {
			try {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(notification.getId()))
					.name("notification")
					.data(NotificationResBody.from(notification)));
			} catch (IOException e) {
				sseEmitterRepository.delete(memberId, emitterId);
				emitter.completeWithError(e);
				return;
			}
		}
	}

	private record ConnectPayload(String message, long unreadCount) {
	}
}
