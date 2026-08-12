package org.example.be.domain.notification.entity;

import java.time.LocalDateTime;

import org.example.be.domain.member.entity.Member;
import org.example.be.domain.notification.type.NotificationType;
import org.example.be.global.entity.Base;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends Base {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	// 리마인더는 유발자가 없어 null. actor가 탈퇴해도 SET NULL로 알림 자체는 유지됨 (message가 스냅샷이라 문구는 안 깨짐)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id")
	private Member actor;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 50)
	private NotificationType type;

	// 발행 시점에 렌더링된 완성 문구를 그대로 저장 (원본 게시글/멤버가 삭제/탈퇴해도 문구가 안 깨지도록 스냅샷으로 보관)
	@Column(name = "message", nullable = false, length = 500)
	private String message;

	// board.id / user_groups.id / schedule.id 중 하나를 가리키는 다형 참조. 실제 대상은 type.targetType으로 해석. FK 미적용
	@Column(name = "target_id")
	private Long targetId;

	// 그룹은 groupId가 아닌 groupName으로 라우팅(/group/{groupName}/detail)되므로 함께 저장. COMMENT/REPLY는 null
	@Column(name = "group_name")
	private String groupName;

	@Column(name = "is_read", nullable = false)
	private Boolean read = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	public static Notification create(Member receiver, Member actor, NotificationType type,
		String message, Long targetId, String groupName) {
		return new Notification(receiver, actor, type, message, targetId, groupName, false, null);
	}

	public void markAsRead(LocalDateTime now) {
		this.read = true;
		this.readAt = now;
	}

}
