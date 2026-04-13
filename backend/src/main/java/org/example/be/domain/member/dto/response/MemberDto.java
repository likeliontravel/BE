package org.example.be.domain.member.dto.response;

import org.example.be.domain.member.entity.Member;

public record MemberDto(
	Long id,
	String email,
	String name,
	String profileImageUrl,
	String role,
	boolean policyAgreed,
	boolean subscribed,
	String oauthProvider,
	boolean shouldChangePassword
) {

	public static MemberDto from(Member member, boolean shouldChangePassword) {
		return new MemberDto(
			member.getId(),
			member.getEmail(),
			member.getName(),
			member.getProfileImageUrl(),
			member.getRole().name(),
			Boolean.TRUE.equals(member.getPolicyAgreed()),
			Boolean.TRUE.equals(member.getSubscribed()),
			member.getOauthProvider() != null
				? member.getOauthProvider().name()
				: null,
			shouldChangePassword
		);
	}

	// from() 오버로딩 추가:
	// ChatMessageService.buildMessageWithProfiles()에서
	// profiles Map을 만들 때 메서드 참조로 Member인자 한개를 받아 DTO로 만들어 toMap 인자로 전달하는데,
	// 위 from 메서드에 shouldChangePassword가 추가되면서 인자 한개만 받는 from을 이용하던 스트림에서 컴파일 에러 발생함. (이거 빌드할 때 에러 났어야 정상인데 왜 발견이 안되었는지 모르것어요)
	public static MemberDto from(Member member) {
		return from(member, false); // shoouldChangePassword 기본값
	}
}

