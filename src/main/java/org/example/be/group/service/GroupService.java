package org.example.be.group.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.UserAuthenticationNotFoundException;
import org.example.be.group.announcement.dto.GroupAnnouncementDTO;
import org.example.be.group.announcement.repository.GroupAnnouncementRepository;
import org.example.be.group.dto.*;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UnifiedUserRepository unifiedUserRepository;
    private final EntityManager entityManager;
    private final GroupAnnouncementRepository groupAnnouncementRepository;
    private final ScheduleRepository scheduleRepository;
    private final SchedulePlaceRepository schedulePlaceRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final AccommodationRepository accommodationRepository;
    private final RestaurantRepository restaurantRepository;

    // 그룹 생성하기
    @Transactional
    public GroupResponseDTO createGroup(GroupCreationRequestDTO request) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        UnifiedUser creator = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new UserAuthenticationNotFoundException("해당 사용자를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.addMember(creator);

        Group savedGroup = groupRepository.save(group);

        return toResponseDTO(savedGroup);
    }

    // 그룹 1개에 대하여 상세 정보 조회하기 - 연결된 일정 및 상세일정( 장소블럭 ), 그룹 멤버들 정보와 해당 프로필들 정보 모두 포함.
    // 그룹 이름, 설명, 창설자명, 창설일 정보, 가장 최근 그룹 공지 반환
    @Transactional(readOnly = true)
    public GroupDetailDTO getGroupDetail(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        // 그룹과 함께 멤버도 같이 패치 조인 셀렉트
        Group group = groupRepository.findWithMembersByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. groupName: " + groupName));

        // 그룹 멤버인지 검증
        if (!isContains(groupName, userIdentifier)) {
            throw new ForbiddenResourceAccessException("그룹의 멤버만 그룹 정보를 조회할 수 있습니다.");
        }

        List<GroupMemberDTO> members = mapToMemberDTOs(group);
        GroupAnnouncementDTO latestAnnouncement = mapToLatestAnnouncementDTO(group);
        GroupScheduleDTO schedule = mapToScheduleDTO(group);

        GroupDetailDTO dto = new GroupDetailDTO();
        dto.setGroupName(group.getGroupName());
        dto.setDescription(group.getDescription());
        dto.setCreatedName(group.getCreatedBy().getName());
        dto.setMembers(members);
        dto.setLatestAnnouncement(latestAnnouncement);
        dto.setSchedule(schedule);

        return dto;
    }


    // 그룹에 멤버 추가 (그룹 초대 구현 후 수정될 수 있음)
    @Transactional
    public void addMemberToGroup(GroupAddMemberRequestDTO request) {
        String groupName = request.getGroupName();
        String userIdentifier = request.getUserIdentifier();

        Group group = getGroupByName(groupName);

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 userIdentifier의 유저를 찾을 수 없습니다. userIdnetifier : " + userIdentifier));

        if (group.getMembers().contains(user)) {
            throw new IllegalArgumentException("이미 그룹에 가입된 사용자입니다.");
        }

        group.addMember(user);
        groupRepository.save(group);
    }

    // 그룹 설명 변경
    public void modifyDescribtion(GroupModifyRequestDTO request) {

        String groupName = request.getGroupName();
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();    // 요청자의 userIdentifier
        String description = request.getDescription();

        // 그룹 내 멤버인지 검증
        if (!isContains(groupName, userIdentifier)) {
            throw new ForbiddenResourceAccessException("이 그룹에 포함되어 있지 않은 멤버입니다.");
        }

        Group group = getGroupByName(groupName);
        group.setDescription(description);
        groupRepository.save(group);
    }

    // 그룹 나가기
    @Transactional
    public void exitGroup(GroupExitOrDeleteRequestDTO request) {
        String groupName = request.getGroupName();
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        Group group = getGroupByName(groupName);

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        if (!group.getMembers().contains(user)) {
            throw new ForbiddenResourceAccessException("이 그룹에 포함되어 있지 않은 멤버입니다.");
        }

        if (userIdentifier.equals(group.getCreatedBy().getUserIdentifier())) {
            throw new IllegalArgumentException("그룹의 창설자는 그룹을 나갈 수 없습니다. 그룹을 삭제하세요.");
        }

        group.removeMember(user);
        entityManager.flush();
    }

    // 그룹 삭제하기 (그룹 생성한 유저만 가능)
    @Transactional
    public void deleteGroup(GroupExitOrDeleteRequestDTO request) {

        String groupName = request.getGroupName();
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        Group group = validateGroupCreator(groupName, userIdentifier);
        groupRepository.delete(group);

        // 채팅 및 일정 연계 삭제 확인해야함
    }

    // 유저가 가입한 그룹 정보 조회하기
    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getAllGroups() {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        List<Group> groups = groupRepository.findByMembersContaining(user);

        if (groups.isEmpty()) {
            throw new NoSuchElementException("해당 유저가 가입한 그룹을 찾을 수 없습니다. userIdentifier: " + userIdentifier);
        }

        return groups.stream()
                .map(this::toResponseDTO)
                .toList();
    }


    // 이 그룹에 해당 멤버가 있는지 조회하기
    @Transactional(readOnly = true)
    public Boolean isContains(String groupName, String userIdentifier) {
        Group group = getGroupByName(groupName);

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        System.out.println("[GroupService에서 검증 로그] 그룹 이름: " + groupName);
        System.out.println("[검증 로그] 요청자 userIdentifier: " + userIdentifier);
        System.out.println("[검증 로그] 그룹 멤버 목록:");
        group.getMembers().forEach(member ->
                System.out.println(" - " + member.getUserIdentifier())
        );

        return group.getMembers().contains(user);   // return; 있다면 true, 없다면 false
    }

    // converter to responseDTO
    private GroupResponseDTO toResponseDTO(Group group) {
        return new GroupResponseDTO(group.getId(), group.getGroupName(), group.getDescription(), group.getCreatedBy().getUserIdentifier());
    }

    // 그룹 창설자 검증
    public Group validateGroupCreator(String groupName, String userIdentifier) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. groupName: " + groupName));

        if (!group.getCreatedBy().getUserIdentifier().equals(userIdentifier)) {
            throw new ForbiddenResourceAccessException("해당 그룹의 창설자만 접근할 수 있습니다.");
        }
        return group;
    }

    // 그룹 명으로 그룹 조회 ( 해당 이름의 그룹이 없다면 예외 발생 )
    @Transactional
    public Group getGroupByName(String groupName) {
        return groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new ForbiddenResourceAccessException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));
    }

    // ==================== 그룹 상세 조회 이용 내부메서드 ====================
    // 멤버 정보 추출해 리스트로 묶어주기
    private List<GroupMemberDTO> mapToMemberDTOs(Group group) {
        return group.getMembers().stream()
                .map(user -> {
                    GroupMemberDTO dto = new GroupMemberDTO();
                    dto.setName(user.getName());
                    dto.setProfileImageUrl(user.getProfileImageUrl());
                    return dto;
                }).toList();
    }

    // 마지막 그룹 공지 추출하기
    private GroupAnnouncementDTO mapToLatestAnnouncementDTO(Group group) {
        return groupAnnouncementRepository.findTopByGroupOrderByTimeStampDesc(group)
                .map(announcement -> {
                    GroupAnnouncementDTO dto = new GroupAnnouncementDTO();
                    dto.setId(announcement.getId());
                    dto.setTitle(announcement.getTitle());
                    dto.setContent(announcement.getContent());
                    dto.setWriterName(announcement.getWriterName());
                    return dto;
                }).orElse(null);
    }

    private GroupScheduleDTO mapToScheduleDTO(Group group) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findByGroup(group);
        if (scheduleOptional.isEmpty()) {
            return null;
        }

        Schedule schedule = scheduleOptional.get();
        List<SchedulePlace> places = schedulePlaceRepository.findBySchedule(schedule);

        List<GroupSchedulePlaceDTO> placeDTOs = places.stream()
                .map(place -> {
                    GroupSchedulePlaceDTO dto = new GroupSchedulePlaceDTO();
                    dto.setContentId(place.getContentId());
                    dto.setPlaceType(place.getPlaceType());
                    dto.setTitle(resolveTitle(place));
                    dto.setAddress(resolveAddress(place));
                    dto.setVisitStart(place.getVisitStart());
                    dto.setVisitEnd(place.getVisitedEnd());
                    dto.setDayOrder(place.getDayOrder());
                    dto.setOrderInDay(place.getOrderInDay());
                    return dto;
                }).toList();

        GroupScheduleDTO dto = new GroupScheduleDTO();
        dto.setStartSchedule(schedule.getStartSchedule());
        dto.setEndSchedule(schedule.getEndSchedule());
        dto.setPlaces(placeDTOs);
        return dto;
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
