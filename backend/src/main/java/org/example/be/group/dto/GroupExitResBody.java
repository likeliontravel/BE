package org.example.be.group.dto;

// 그룹 나가기 응답용 ResponseBody
public record GroupExitResBody(
	Long exitedMemberId,
	String exitedMemberEmail,
	String exitedMemberName,
	String groupName
) {
}
