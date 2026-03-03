package org.example.be.group.invitation.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.example.be.exception.custom.InvalidInvitationException;
import org.example.be.group.entitiy.Group;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.repository.GroupInvitationRepository;
import org.example.be.group.repository.GroupRepository;
import org.example.be.group.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupInvitationService {

	private final GroupInvitationRepository invitationRepository;
	private final GroupRepository groupRepository;
	private final GroupService groupService;
	private static final long INVITATION_VALID_HOURS = 24; // 초대코드가 만료될 시간 24으로 초기화

	// 초대 링크 조회 ( 만료된 링크라면 무효화하고 메시지 반환 )
	@Transactional
	public GroupInvitation getValidOrExpireInvitation(String groupName, Long memberId) {
		Group group = groupService.validateGroupCreator(groupName, memberId);

		LocalDateTime now = LocalDateTime.now();
		Optional<GroupInvitation> validOptional = invitationRepository.findByGroupAndActiveTrueAndExpiresAtAfter(group,
			now);

		if (validOptional.isPresent()) {
			return validOptional.get();
		}

		// 기존에 존재하는 활성화 초대 링크가 만료되었는지 확인
		Optional<GroupInvitation> expiredOptional = invitationRepository.findByGroupAndActiveTrue(group);
		// 유효기간이 지난 링크인 경우 비활성화
		expiredOptional.ifPresent(expired -> {
			expired.setActive(false);
			invitationRepository.save(expired);
		});

		if (expiredOptional.isPresent()) {
			throw new InvalidInvitationException("초대 링크가 만료되었습니다. 새로 생성하세요.");
		} else {
			throw new NoSuchElementException("초대 링크가 없습니다. 초대 링크를 생성하세요.");
		}
	}

	// 초대 링크 (강제) 생성 - 초대 링크가 없거나, 유효한 초대 링크가 있더라도 기존 링크를 무효화하고 새로 만들고 싶을 경우
	@Transactional
	public GroupInvitation forceGenerateNewInvitation(String groupName, Long memberId) {
		Group group = groupService.validateGroupCreator(groupName, memberId);

		invitationRepository.findByGroupAndActiveTrue(group)
			.ifPresent(existing -> {
				existing.setActive(false);
				invitationRepository.save(existing);
			});

		GroupInvitation invitation = new GroupInvitation();
		invitation.setGroup(group);
		invitation.setCreatedAt(LocalDateTime.now());
		invitation.setExpiresAt(LocalDateTime.now().plusHours(INVITATION_VALID_HOURS));
		invitation.setActive(true);
		invitation.setInvitationCode(UUID.randomUUID().toString().replace("-", ""));

		return invitationRepository.save(invitation);
	}

	// 초대 코드를 받아 해당 그룹 초대링크객체를 반환해주는 메서드
	@Transactional(readOnly = true)
	public GroupInvitation getValidInvitation(String invitationCode) {
		LocalDateTime now = LocalDateTime.now();
		return invitationRepository.findByInvitationCodeAndActiveTrueAndExpiresAtAfter(invitationCode, now)
			.orElseThrow(() -> new InvalidInvitationException("초대 링크가 유효하지 않거나 만료되었습니다. 초대 코드: " + invitationCode));
	}

}
