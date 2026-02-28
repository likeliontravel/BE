package org.example.be.group.announcement.dto;

import java.time.LocalDateTime;

// GroupDetail 내부에 중첩되는 최신 공지 요약 DTO (groupName 미포함)
public record GroupAnnouncementSummaryDTO(
	Long id,
	String title,
	String content,
	String writerName,
	LocalDateTime timeStamp
) {
}
