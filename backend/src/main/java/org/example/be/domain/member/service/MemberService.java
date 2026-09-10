package org.example.be.domain.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.be.domain.board.repository.BoardRepository;
import org.example.be.domain.board.repository.CommentRepository;
import org.example.be.domain.chat.repository.ChatMessageRepository;
import org.example.be.domain.group.announcement.repository.GroupAnnouncementRepository;
import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.invitation.repository.GroupInvitationRepository;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.dto.request.MemberJoinReqBody;
import org.example.be.domain.member.dto.request.PasswordUpdateReqBody;
import org.example.be.domain.member.dto.response.MemberProfileResBody;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.domain.member.type.OauthProvider;
import org.example.be.domain.notification.service.NotificationCleanupService;
import org.example.be.domain.notification.sse.SseEmitterRepository;
import org.example.be.domain.schedule.entity.Schedule;
import org.example.be.domain.schedule.repository.ScheduleRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.storage.gcs.GCSService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final GCSService gcsService;
	private final GroupRepository groupRepository;
	private final GroupInvitationRepository groupInvitationRepository;
	private final GroupAnnouncementRepository groupAnnouncementRepository;
	private final BoardRepository boardRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final CommentRepository commentRepository;
	private final ScheduleRepository scheduleRepository;
	private final NotificationCleanupService notificationCleanupService;
	private final SseEmitterRepository sseEmitterRepository;

	public Member join(MemberJoinReqBody reqBody) {
		if (memberRepository.existsByEmail(reqBody.email())) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED, "중복 가입 시도 - email: " + reqBody.email());
		}
		String password = passwordEncoder.encode(reqBody.password());
		Member member = Member.createForJoin(reqBody.email(), reqBody.name(), password);
		return memberRepository.save(member);
	}

	public Member authenticateAndGetMember(String email, String password) {
		// 계정 존재 여부를 노출하지 않기 위해 비밀번호 불일치(checkPassword)와 같은 LOGIN_FAILED 를 쓴다.
		// 구분에 필요한 정보는 두 번째 인자(debugMessage)로 로그에만 남는다.
		Member member = findByEmail(email)
			.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED, "존재하지 않는 이메일 로그인 시도 - email: " + email));
		checkPassword(member, password);
		return member;
	}

	// 비밀번호 초기화 로직
	@Transactional
	public void resetPassword(String email, String newPassword) {
		Member member = findByEmail(email)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "사용자를 찾을 수 없습니다. email: " + email));

		String newEncodedPassword = passwordEncoder.encode(newPassword);
		member.changePassword(newEncodedPassword);

		memberRepository.save(member);
	}

	// 비밀번호 변경 로직
	@Transactional
	public void updatePassword(Long memberId, PasswordUpdateReqBody reqBody) {
		Member member = getById(memberId);

		checkPassword(member, reqBody.oldPassword());

		String newEncodedPassword = passwordEncoder.encode(reqBody.newPassword());
		member.changePassword(newEncodedPassword);

		memberRepository.save(member);
	}

	@Transactional
	public void updateName(long memberId, String name) {
		Member member = getById(memberId);

		member.changeName(name);

		memberRepository.save(member);
	}

	@Transactional
	public String updateProfileImageUrl(Long memberId, MultipartFile file) {
		Member member = getById(memberId);

		if (member.getProfileImageUrl() != null) {
			gcsService.deleteProfileImage(member.getProfileImageUrl());
		}

		String profileImageUrl = gcsService.uploadProfileImage(file, memberId);
		member.updateProfileImageUrl(profileImageUrl);

		return profileImageUrl;
	}

	@Transactional
	public void deleteProfileImage(long memberId) {
		Member member = getById(memberId);

		if (member.getProfileImageUrl() != null) {
			gcsService.deleteProfileImage(member.getProfileImageUrl());
			member.updateProfileImageUrl(null);
		}
	}

	@Transactional
	public void updatePolicyAgreed(long memberId, boolean policyAgreed) {
		Member member = getById(memberId);
		member.updatePolicyAgreed(policyAgreed);
	}

	@Transactional
	public void updateSubscribed(long memberId, boolean subscribed) {
		Member member = getById(memberId);
		member.updateSubscribed(subscribed);
	}

	@Transactional
	public void deleteMember(long memberId) {
		Member member = getById(memberId);

		List<Group> ownedGroups = groupRepository.findAllByCreatedBy(member);
		for (Group group : ownedGroups) {
			Long scheduleId = scheduleRepository.findByGroup(group).map(Schedule::getId).orElse(null);
			notificationCleanupService.deleteByGroup(group.getId(), scheduleId);

			chatMessageRepository.deleteByGroup(group);
			scheduleRepository.findByGroup(group).ifPresent(scheduleRepository::delete);
			groupInvitationRepository.deleteByGroup(group);
			groupAnnouncementRepository.deleteByGroup(group);
			groupRepository.delete(group);
		}

		List<Group> joinedGroups = groupRepository.findByMembersContaining(member);
		for (Group group : joinedGroups) {
			group.removeMember(member);
		}
		groupRepository.flush();

		chatMessageRepository.deleteBySender(member);
		commentRepository.deleteByWriter(member);
		boardRepository.deleteByWriter(member);

		if (member.getProfileImageUrl() != null) {
			gcsService.deleteProfileImage(member.getProfileImageUrl());
		}

		sseEmitterRepository.deleteAllByMemberId(memberId);
		notificationCleanupService.deleteByMember(memberId);

		memberRepository.delete(member);
	}

	public MemberProfileResBody getMemberProfile(Long memberId) {
		Member member = getById(memberId);
		return MemberProfileResBody.from(member);
	}

	public Optional<Member> findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public void checkPassword(Member member, String password) {
		// 소셜 가입 계정은 password가 비어 있어서 그대로 인코더에 넘기면
		// IllegalArgumentException("There is no PasswordEncoder mapped for the id \"null\"") 이 나고
		// catch-all 에 잡혀 500이 된다. 예외를 잡아 번역하지 말고 사전 조건으로 막는다.
		if (member.getPassword() == null || member.getPassword().isBlank()) {
			throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_LOGIN_REQUIRED,
				"소셜 가입 계정의 이메일 로그인 시도 - email: " + member.getEmail());
		}

		if (!passwordEncoder.matches(password, member.getPassword())) {
			// '없는 이메일'(authenticateAndGetMember)과 같은 LOGIN_FAILED 를 쓴다.
			// 문구가 갈리면 계정 존재 여부가 응답으로 노출된다.
			throw new BusinessException(ErrorCode.LOGIN_FAILED,
				"비밀번호 불일치 - email: " + member.getEmail());
		}
	}

	public Member getById(long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "memberId: " + id));
	}

	public boolean isPasswordExpired(Member member) {
		if (member.getOauthProvider() != OauthProvider.General) {
			return false; // 일반 회원이 아닌 경우(소셜 로그인) 비밀번호 변경 권장 대상 아님
		}

		return member.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(30));
	}

}
