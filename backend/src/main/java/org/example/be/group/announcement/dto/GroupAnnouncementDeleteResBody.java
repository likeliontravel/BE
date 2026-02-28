package org.example.be.group.announcement.dto;

// 그룹 공지 삭제 응답용 ResponseBody
public record GroupAnnouncementDeleteResBody(
	Long id,
	String title,
	String content,
	String writerName
) {
}
