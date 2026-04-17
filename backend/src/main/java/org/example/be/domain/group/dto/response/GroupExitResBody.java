package org.example.be.domain.group.dto.response;

// 그룹 나가기 응답용 ResponseBody
public record GroupExitResBody(
	Long exitedMemberId,
	String exitedMemberEmail,
	String exitedMemberName,
	String groupName
) {
}
