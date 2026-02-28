package org.example.be.group.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.group.announcement.dto.GroupAnnouncementSummaryDTO;
import org.example.be.group.announcement.repository.GroupAnnouncementRepository;
import org.example.be.group.dto.GroupAddMemberResBody;
import org.example.be.group.dto.GroupCreateReqBody;
import org.example.be.group.dto.GroupDeleteResBody;
import org.example.be.group.dto.GroupDetailResBody;
import org.example.be.group.dto.GroupExitOrDeleteReqBody;
import org.example.be.group.dto.GroupExitResBody;
import org.example.be.group.dto.GroupMemberDTO;
import org.example.be.group.dto.GroupModifyReqBody;
import org.example.be.group.dto.GroupModifyResBody;
import org.example.be.group.dto.GroupResBody;
import org.example.be.group.dto.GroupScheduleDTO;
import org.example.be.group.dto.GroupSchedulePlaceDTO;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.member.entity.Member;
import org.example.be.member.service.MemberService;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
	private final MemberService memberService;
	private final EntityManager entityManager;
	private final GroupAnnouncementRepository groupAnnouncementRepository;
	private final ScheduleRepository scheduleRepository;
	private final SchedulePlaceRepository schedulePlaceRepository;
	private final TouristSpotRepository touristSpotRepository;
	private final AccommodationRepository accommodationRepository;
	private final RestaurantRepository restaurantRepository;

	// 그룹 생성하기
	@Transactional
	public GroupResBody createGroup(GroupCreateReqBody request, Long memberId) {
		String groupName = request.groupName();

		groupRepository.findByGroupName(groupName).ifPresent(group -> {
			throw new IllegalArgumentException("이미 존재하는 그룹 이름입니다: " + groupName);
		});

		Member creator = memberService.getById(memberId);

		Group group = new Group();
		group.setGroupName(request.groupName());
		group.setDescription(request.description());
		group.setCreatedBy(creator);
		group.addMember(creator);

		Group savedGroup = groupRepository.save(group);

		return toResBody(savedGroup);
	}

	// 그룹 1개에 대하여 상세 정보 조회하기 - 연결된 일정 및 상세일정( 장소블럭 ), 그룹 멤버들 정보와 해당 프로필들 정보 모두 포함.
	// 그룹 이름, 설명, 창설자명, 창설일 정보, 가장 최근 그룹 공지 반환
	@Transactional(readOnly = true)
	public GroupDetailResBody getGroupDetail(String groupName, Long memberId) {
		// 그룹과 함께 멤버도 같이 패치 조인 셀렉트
		Group group = groupRepository.findWithMembersByGroupName(groupName)
			.orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. groupName: " + groupName));

		// 그룹 멤버인지 검증
		if (!isContains(groupName, memberId)) {
			throw new ForbiddenResourceAccessException("그룹의 멤버만 그룹 정보를 조회할 수 있습니다.");
		}

		List<GroupMemberDTO> members = mapToMemberResBodyList(group);
		GroupAnnouncementSummaryDTO latestAnnouncement = mapToLatestAnnouncementResBody(group);
		GroupScheduleDTO schedule = mapToScheduleResBody(group);

		return new GroupDetailResBody(
			group.getGroupName(),
			group.getDescription(),
			group.getCreatedBy().getName(),
			members,
			latestAnnouncement,
			schedule
		);
	}

	// 그룹에 멤버 추가 ( 초대 코드를 통한 가입 )
	@Transactional
	public GroupAddMemberResBody addMemberToGroup(String groupName, Long memberId) {
		Group group = getGroupByName(groupName);

		Member user = memberService.getById(memberId);

		if (group.getMembers().contains(user)) {
			throw new IllegalArgumentException("이미 그룹에 가입된 사용자입니다.");
		}

		group.addMember(user);
		groupRepository.save(group);

		return new GroupAddMemberResBody(user.getId(), user.getEmail(), user.getName(), groupName);
	}

	// 그룹 설명 변경
	@Transactional
	public GroupModifyResBody modifyDescribtion(GroupModifyReqBody request, Long memberId) {
		String groupName = request.groupName();
		String description = request.description();

		// 그룹 내 멤버인지 검증
		if (!isContains(groupName, memberId)) {
			throw new ForbiddenResourceAccessException("이 그룹에 포함되어 있지 않은 멤버입니다.");
		}

		Group group = getGroupByName(groupName);
		group.setDescription(description);
		groupRepository.save(group);

		return new GroupModifyResBody(groupName, description);
	}

	// 그룹 나가기
	@Transactional
	public GroupExitResBody exitGroup(GroupExitOrDeleteReqBody request, Long memberId) {
		String groupName = request.groupName();

		Group group = getGroupByName(groupName);

		Member user = memberService.getById(memberId);

		if (!group.getMembers().contains(user)) {
			throw new ForbiddenResourceAccessException("이 그룹에 포함되어 있지 않은 멤버입니다.");
		}

		if (memberId.equals(group.getCreatedBy().getId())) {
			throw new IllegalArgumentException("그룹의 창설자는 그룹을 나갈 수 없습니다. 그룹을 삭제하세요.");
		}

		group.removeMember(user);
		entityManager.flush();

		return new GroupExitResBody(user.getId(), user.getEmail(), user.getName(), groupName);
	}

	// 그룹 삭제하기 (그룹 생성한 유저만 가능)
	@Transactional
	public GroupDeleteResBody deleteGroup(GroupExitOrDeleteReqBody request, Long memberId) {
		String groupName = request.groupName();

		Group group = validateGroupCreator(groupName, memberId);
		groupRepository.delete(group);

		return new GroupDeleteResBody(groupName);

		// 채팅 및 일정 연계 삭제 확인해야함
	}

	// 유저가 가입한 그룹 정보 조회하기
	@Transactional(readOnly = true)
	public List<GroupResBody> getAllGroups(Long memberId) {
		Member user = memberService.getById(memberId);

		List<Group> groups = groupRepository.findByMembersContaining(user);

		if (groups.isEmpty()) {
			throw new NoSuchElementException("해당 유저가 가입한 그룹을 찾을 수 없습니다. memberId: " + memberId);
		}

		return groups.stream()
			.map(this::toResBody)
			.toList();
	}

	// 이 그룹에 해당 멤버가 있는지 조회하기
	@Transactional(readOnly = true)
	public Boolean isContains(String groupName, Long memberId) {
		Group group = groupRepository.findWithMembersByGroupName(groupName)
			.orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다. groupName: " + groupName));

		System.out.println("[GroupService에서 검증 로그] 그룹 이름: " + groupName);
		System.out.println("[검증 로그] 요청자 memberId: " + memberId);
		System.out.println("[검증 로그] 그룹 멤버 목록:");
		group.getMembers().forEach(member ->
			System.out.println(" - " + member.getId())
		);

		return group.getMembers().stream().anyMatch(m -> m.getId().equals(memberId));   // return; 있다면 true, 없다면 false
	}

	// Convert to GroupResponseBody
	private GroupResBody toResBody(Group group) {
		return new GroupResBody(
			group.getId(),
			group.getGroupName(),
			group.getDescription(),
			group.getCreatedBy().getId()
		);
	}

	// 그룹 창설자 검증
	public Group validateGroupCreator(String groupName, Long memberId) {
		Group group = groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. groupName: " + groupName));

		if (!group.getCreatedBy().getId().equals(memberId)) {
			throw new ForbiddenResourceAccessException("해당 그룹의 창설자만 접근할 수 있습니다.");
		}
		return group;
	}

	// 그룹 명으로 그룹 조회 ( 해당 이름의 그룹이 없다면 예외 발생 )
	@Transactional
	public Group getGroupByName(String groupName) {
		return groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new NoSuchElementException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));
	}

	// ==================== 그룹 상세 조회 이용 내부메서드 ====================
	// 멤버 정보 추출해 리스트로 묶어주기
	private List<GroupMemberDTO> mapToMemberResBodyList(Group group) {
		return group.getMembers().stream()
			.map(user -> new GroupMemberDTO(user.getName(), user.getProfileImageUrl()))
			.toList();
	}

	// 마지막 그룹 공지 추출하기
	private GroupAnnouncementSummaryDTO mapToLatestAnnouncementResBody(Group group) {
		return groupAnnouncementRepository.findTopByGroupOrderByTimeStampDesc(group)
			.map(announcement -> new GroupAnnouncementSummaryDTO(
				announcement.getId(),
				announcement.getTitle(),
				announcement.getContent(),
				announcement.getWriterName(),
				announcement.getTimeStamp()
			)).orElse(null);
	}

	// 해당 그룹의 스케줄 추출하기
	private GroupScheduleDTO mapToScheduleResBody(Group group) {
		Optional<Schedule> scheduleOptional = scheduleRepository.findByGroup(group);
		if (scheduleOptional.isEmpty()) {
			return null;
		}

		Schedule schedule = scheduleOptional.get();
		List<SchedulePlace> places = schedulePlaceRepository.findBySchedule(schedule);

		List<GroupSchedulePlaceDTO> placeResBodyList = places.stream()
			.map(place -> new GroupSchedulePlaceDTO(
				place.getContentId(),
				place.getPlaceType(),
				resolveTitle(place),
				resolveAddress(place),
				place.getVisitStart(),
				place.getVisitedEnd(),
				place.getDayOrder(),
				place.getOrderInDay()
			)).toList();

		return new GroupScheduleDTO(
			schedule.getStartSchedule(),
			schedule.getEndSchedule(),
			placeResBodyList
		);
	}

	private String resolveTitle(SchedulePlace place) {
		return switch (place.getPlaceType()) {
			case TouristSpot -> touristSpotRepository.findByContentId(place.getContentId())
				.map(t -> t.getTitle()).orElse("(없거나 삭제된 장소)");
			case Restaurant -> restaurantRepository.findByContentId(place.getContentId())
				.map(r -> r.getTitle()).orElse("(없거나 삭제된 장소)");
			case Accommodation -> accommodationRepository.findByContentId(place.getContentId())
				.map(a -> a.getTitle()).orElse("(없거나 삭제된 장소)");
		};
	}

	private String resolveAddress(SchedulePlace place) {
		return switch (place.getPlaceType()) {
			case TouristSpot -> touristSpotRepository.findByContentId(place.getContentId())
				.map(t -> t.getAddr1() + t.getAddr2()).orElse("(없거나 삭제된 장소)");
			case Restaurant -> restaurantRepository.findByContentId(place.getContentId())
				.map(r -> r.getAddr1() + r.getAddr2()).orElse("(없거나 삭제된 장소)");
			case Accommodation -> accommodationRepository.findByContentId(place.getContentId())
				.map(a -> a.getAddr1() + a.getAddr2()).orElse("(없거나 삭제된 장소)");
		};
	}
}
