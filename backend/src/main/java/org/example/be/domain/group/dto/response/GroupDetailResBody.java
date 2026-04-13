package org.example.be.domain.group.dto.response;

import java.util.List;

import org.example.be.domain.group.announcement.dto.GroupAnnouncementSummaryDTO;
import org.example.be.domain.group.dto.GroupMemberDTO;
import org.example.be.domain.group.dto.GroupScheduleDTO;

// 그룹 상세 정보 응답용 ResponseBody (멤버 목록, 최신 공지 요약, 일정 포함)
public record GroupDetailResBody(
	String groupName,
	String description,
	String createdName,
	List<GroupMemberDTO> members,
	GroupAnnouncementSummaryDTO latestAnnouncement,
	GroupScheduleDTO schedule
) {
}
