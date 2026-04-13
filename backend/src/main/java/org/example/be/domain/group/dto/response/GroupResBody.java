package org.example.be.domain.group.dto.response;

// 그룹 정보 응답용 Response Body
public record GroupResBody(
	Long id,
	String groupName,
	String description,
	Long createdByMemberId
) {
}
