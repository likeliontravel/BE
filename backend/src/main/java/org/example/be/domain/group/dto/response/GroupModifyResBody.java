package org.example.be.domain.group.dto.response;

// 그룹 설명 수정 응답용 ResponseBody
public record GroupModifyResBody(
	String groupName,
	String description
) {
}
