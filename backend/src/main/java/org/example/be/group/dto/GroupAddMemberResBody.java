package org.example.be.group.dto;

// 그룹 멤버 추가 응답용 ResponseBody
public record GroupAddMemberResBody(
	Long joinedMemberId,
	String joinedMemberEmail,
	String joinedMemberName,
	String groupName
) {
}
