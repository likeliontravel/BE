package org.example.be.group.dto;

// 그룹 정보 응답용 Response Body
public record GroupResBody(
	Long id,
	String groupName,
	String description,
	Long createdByMemberId
) {
}
