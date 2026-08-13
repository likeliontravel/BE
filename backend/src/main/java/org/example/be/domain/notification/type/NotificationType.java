package org.example.be.domain.notification.type;

import lombok.Getter;

@Getter
public enum NotificationType {
	COMMENT("%s님이 회원님의 게시글에 댓글을 달았습니다.", TargetType.BOARD, 1),

	REPLY("%s님이 회원님의 댓글에 답글을 달았습니다.", TargetType.BOARD, 1),

	GROUP_JOIN("%s님이 '%s' 그룹에 참여했습니다.", TargetType.GROUP, 2),

	SCHEDULE_D_1("'%s' 그룹의 여행이 내일 시작됩니다.", TargetType.SCHEDULE, 1),

	SCHEDULE_D_DAY("'%s' 그룹의 여행이 오늘 시작됩니다.", TargetType.SCHEDULE, 1);

	//알림 문구	템플릿, 어떤 문구가 채워지는지
	private final String template;

	//targetId가 무엇을 가리키는지, 프론트 라우팅 분기 기준
	private final TargetType targetType;

	private final int argCount;

	NotificationType(String template, TargetType targetType, int argCount) {
		this.template = template;
		this.targetType = targetType;
		this.argCount = argCount;
	}

	//BOARD -> board.id, GROUP -> user_groups.id, SCHEDULE -> schedule.id
	public enum TargetType {
		BOARD, GROUP, SCHEDULE
	}
}
