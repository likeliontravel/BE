package org.example.be.domain.notification.service;

import java.util.List;

import org.example.be.domain.notification.repository.NotificationRepository;
import org.example.be.domain.notification.type.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

	private static final List<NotificationType> SCHEDULE_TYPES =
		List.of(NotificationType.SCHEDULE_D_1, NotificationType.SCHEDULE_D_DAY);

	private final NotificationRepository notificationRepository;

	// 그룹 삭제 시 그 그룹 가입 알림 + 그 그룹 일정의 리마인더 알림 정리 (targetId가 다형 참조라 FK로 자동 정리되지 않음)
	@Transactional
	public void deleteByGroup(Long groupId, Long scheduleId) {
		notificationRepository.deleteByTypeInAndTargetId(List.of(NotificationType.GROUP_JOIN), groupId);
		if (scheduleId != null) {
			notificationRepository.deleteByTypeInAndTargetId(SCHEDULE_TYPES, scheduleId);
		}
	}

	// 회원 탈퇴 시 그 회원이 받은 알림 정리 (receiver_id는 ON DELETE CASCADE라 안전망이지만 관례상 명시 호출)
	@Transactional
	public void deleteByMember(Long memberId) {
		notificationRepository.deleteByReceiverId(memberId);
	}
}
