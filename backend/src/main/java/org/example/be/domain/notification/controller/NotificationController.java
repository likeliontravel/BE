package org.example.be.domain.notification.controller;

import org.example.be.domain.notification.dto.response.NotificationListResBody;
import org.example.be.domain.notification.dto.response.NotificationUnreadCountResBody;
import org.example.be.domain.notification.service.NotificationService;
import org.example.be.domain.notification.sse.SseEmitterService;
import org.example.be.global.response.CommonResponse;
import org.example.be.global.security.config.SecurityUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

	private final NotificationService notificationService;
	private final SseEmitterService sseEmitterService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@AuthenticationPrincipal SecurityUser user,
		@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId, HttpServletResponse response) {
		response.setHeader("X-Accel-Buffering", "no");
		response.setHeader("Cache-Control", "no-cache");
		return sseEmitterService.subscribe(user.getId(), lastEventId);
	}

	@GetMapping
	public ResponseEntity<CommonResponse<NotificationListResBody>> getNotifications(
		@AuthenticationPrincipal SecurityUser user, @RequestParam(required = false) Long lastNotificationId,
		@RequestParam(defaultValue = "20") int size) {

		NotificationListResBody resBody = notificationService.getNotifications(user.getId(), lastNotificationId, size);

		return ResponseEntity.ok(CommonResponse.success(resBody, "알림 목록 조회 성공"));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<CommonResponse<NotificationUnreadCountResBody>> getUnreadCount(
		@AuthenticationPrincipal SecurityUser user) {

		NotificationUnreadCountResBody resBody = notificationService.getUnreadCount(user.getId());
		return ResponseEntity.ok(CommonResponse.success(resBody, "안읽은 알림 개수 조회 성공"));
	}

	@PatchMapping("/{id}/read")
	public ResponseEntity<CommonResponse<Void>> markAsRead(@PathVariable Long id,
		@AuthenticationPrincipal SecurityUser user) {

		notificationService.markAsRead(id, user.getId());
		return ResponseEntity.ok(CommonResponse.success(null, "알림을 읽음 처리했습니다."));
	}

	@PatchMapping("/read-all")
	public ResponseEntity<CommonResponse<Integer>> markAllAsRead(@AuthenticationPrincipal SecurityUser user) {
		int count = notificationService.markAllAsRead(user.getId());
		return ResponseEntity.ok(CommonResponse.success(count, "모든 알림을 읽음 처리했습니다."));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<CommonResponse<Void>> deleteNotification(@PathVariable Long id,
		@AuthenticationPrincipal SecurityUser user) {

		notificationService.deleteNotification(id, user.getId());
		return ResponseEntity.ok(CommonResponse.success(null, "알림을 삭제했습니다."));
	}

}

