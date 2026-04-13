package org.example.be.domain.group.announcement.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.be.domain.member.service.MemberService;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.domain.group.announcement.dto.GroupAnnouncementCreateReqBody;
import org.example.be.domain.group.announcement.dto.GroupAnnouncementDeleteReqBody;
import org.example.be.domain.group.announcement.dto.GroupAnnouncementDeleteResBody;
import org.example.be.domain.group.announcement.dto.GroupAnnouncementResBody;
import org.example.be.domain.group.announcement.entity.GroupAnnouncement;
import org.example.be.domain.group.announcement.repository.GroupAnnouncementRepository;
import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.group.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupAnnouncementService {

	private final GroupRepository groupRepository;
	private final GroupService groupService;
	private final GroupAnnouncementRepository groupAnnouncementRepository;
	private final MemberService memberService;

	// 그룹 공지 생성
	@Transactional
	public GroupAnnouncementResBody createGroupAnnouncement(
		GroupAnnouncementCreateReqBody request,
		Long memberId) {
		// 저장할 정보 가져오기
		String rawGroupName = request.groupName();
		String groupName =
			needsDecoding(rawGroupName) ? URLDecoder.decode(rawGroupName, StandardCharsets.UTF_8) : rawGroupName;

		Group group = groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "groupName: " + groupName));

		if (!groupService.isContains(groupName, memberId)) {
			throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND, " groupName: " + groupName);
		}

		GroupAnnouncement newAnnouncement = new GroupAnnouncement();
		newAnnouncement.setGroup(group);
		newAnnouncement.setTitle(request.title());
		newAnnouncement.setContent(request.content());
		newAnnouncement.setTimeStamp(LocalDateTime.now());
		newAnnouncement.setWriterName(memberService.getById(memberId).getName());

		return toResBody(groupAnnouncementRepository.save(newAnnouncement));
	}

	// 최상단 노출 그룹 공지 1개만 조회
	@Transactional(readOnly = true)
	public GroupAnnouncementResBody getLatestAnnouncement(String groupName, Long memberId) {
		Group group = groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "groupName: " + groupName));

		if (!groupService.isContains(groupName, memberId)) {
			throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND,
				" groupName: " + groupName + ", memberId: " + memberId);
		}

		Optional<GroupAnnouncement> latestAnnouncementOptional = groupAnnouncementRepository.findTopByGroupOrderByTimeStampDesc(
			group);

		if (latestAnnouncementOptional.isEmpty()) {
			throw new BusinessException(ErrorCode.GROUP_ANNOUNCEMENT_LATEST_NOT_FOUND, "groupName: " + groupName);
		}

		return toResBody(latestAnnouncementOptional.get());
	}

	// 그룹 공지 전부 조회 (최신순 정렬되어 반환됨)
	@Transactional(readOnly = true)
	public List<GroupAnnouncementResBody> getAllGroupAnnouncements(String groupName, Long memberId) {
		// 요청자가 그룹 멤버인지 검증
		if (!groupService.isContains(groupName, memberId)) {
			throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND,
				" groupName: " + groupName + ", memberId: " + memberId);
		}

		Optional<Group> groupOptional = groupRepository.findByGroupName(groupName);
		// 해당 이름의 그룹이 존재하는지 확인
		if (groupOptional.isEmpty()) {
			throw new BusinessException(ErrorCode.GROUP_NOT_FOUND, "groupName: " + groupName);
		}

		Group group = groupOptional.get();
		List<GroupAnnouncement> announcements = groupAnnouncementRepository.findAllByGroupOrderByTimeStampDesc(group);

		// 공지가 있다면 공지(들)을 리스트로, 없다면 빈 리스트 반환
		return announcements.stream().map(this::toResBody).collect(Collectors.toList());
	}

	// 그룹 공지 삭제
	@Transactional
	public GroupAnnouncementDeleteResBody deleteGroupAnnouncement(GroupAnnouncementDeleteReqBody request,
		Long memberId) {
		String rawGroupName = request.groupName();
		String groupName =
			needsDecoding(rawGroupName) ? URLDecoder.decode(rawGroupName, StandardCharsets.UTF_8) : rawGroupName;

		GroupAnnouncement groupAnnouncement = groupAnnouncementRepository.findById(request.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_ANNOUNCEMENT_NOT_FOUND,
				"groupAnnouncementId: " + request.id()));

		// 요청한 그룹이 공지의 그룹과 일치하는지 확인
		if (!groupAnnouncement.getGroup().getGroupName().equals(groupName)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "삭제하려는 공지가 요청한 그룹의 공지가 아닙니다."
				+ "\n요청한 groupName: " + groupName
				+ "\n삭제하려는 공지의 groupName: " + groupAnnouncement.getGroup().getGroupName());
		}

		// 요청자가 해당 그룹의 멤버인지 확인
		if (!groupService.isContains(groupName, memberId)) {
			throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND,
				"groupName: " + groupName + ", memberId: " + memberId);
		}

		GroupAnnouncementDeleteResBody deletedInfo = new GroupAnnouncementDeleteResBody(
			groupAnnouncement.getId(),
			groupAnnouncement.getTitle(),
			groupAnnouncement.getContent(),
			groupAnnouncement.getWriterName()
		);

		// 공지 삭제
		groupAnnouncementRepository.delete(groupAnnouncement);

		return deletedInfo;
	}

	// 문자열이 URL 인코딩 상태인지 검사. 디코딩이 필요한 경우 true 반환
	private boolean needsDecoding(String s) {
		return s != null && s.contains("%");
	}

	// ResponseDTO로 파싱
	public GroupAnnouncementResBody toResBody(GroupAnnouncement entity) {
		return new GroupAnnouncementResBody(
			entity.getId(),
			entity.getGroup().getGroupName(),
			entity.getTitle(),
			entity.getContent(),
			entity.getTimeStamp(),
			entity.getWriterName()
		);
	}

}
