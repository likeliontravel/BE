package org.example.be.group.announcement.dto;

import java.time.LocalDateTime;

// 공지 API 직접 응답용 ResponseBody (groupName 포함 6필드)
public record GroupAnnouncementResBody(
	Long id,
	String groupName,
	String title,
	String content,
	LocalDateTime timeStamp,
	String writerName
) {
}
