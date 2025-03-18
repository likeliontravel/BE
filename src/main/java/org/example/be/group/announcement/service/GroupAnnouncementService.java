package org.example.be.group.announcement.service;

import lombok.RequiredArgsConstructor;
import org.example.be.group.announcement.dto.GroupAnnouncementCreationRequestDTO;
import org.example.be.group.announcement.dto.GroupAnnouncementDeleteRequestDTO;
import org.example.be.group.announcement.dto.GroupAnnouncementResponseDTO;
import org.example.be.group.announcement.entity.GroupAnnouncement;
import org.example.be.group.announcement.repository.GroupAnnouncementRepository;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.group.service.GroupService;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupAnnouncementService {

    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final GroupAnnouncementRepository groupAnnouncementRepository;
    private final UnifiedUserService unifiedUserService;

    // 그룹 공지 생성
    public GroupAnnouncementResponseDTO createGroupAnnouncement(GroupAnnouncementCreationRequestDTO groupAnnouncementCreationRequestDTO) {
        // 저장할 정보 가져오기
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        String groupName = groupAnnouncementCreationRequestDTO.getGroupName();
        String title = groupAnnouncementCreationRequestDTO.getTitle();
        String content = groupAnnouncementCreationRequestDTO.getContent();
        LocalDateTime timeStamp = LocalDateTime.now();

        // 작성자가 그룹 멤버인지 검증
        Optional<Group> groupOptional = groupRepository.findByGroupName(groupName);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            if (groupService.isContains(groupName, userIdentifier)) {
                GroupAnnouncement newGroupAnnouncement = new GroupAnnouncement();
                newGroupAnnouncement.setGroup(group);
                newGroupAnnouncement.setTitle(title);
                newGroupAnnouncement.setContent(content);
                newGroupAnnouncement.setTimeStamp(timeStamp);
                newGroupAnnouncement.setWriterName(
                        unifiedUserService.getNameByUserIdentifier(userIdentifier)
                );
                GroupAnnouncement groupAnnouncement = groupAnnouncementRepository.save(newGroupAnnouncement);
                return toResponseDTO(groupAnnouncement);
            } else {
                throw new IllegalArgumentException("요청자가 해당 그룹의 멤버가 아닙니다.");
            }
        } else {
            throw new IllegalArgumentException("그룹을 찾을 수 없습니다. groupName: " + groupName);
        }

    }

    // 최상단 노출 그룹 공지 1개만 조회
    public GroupAnnouncementResponseDTO getLatestAnnouncement(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        // 요청자가 그룹 멤버인지 검증
        if (groupService.isContains(groupName, userIdentifier)) {
            Optional<Group> groupOptional = groupRepository.findByGroupName(groupName);
            if (groupOptional.isPresent()) {
                Group group = groupOptional.get();
                GroupAnnouncement latestGroupAnnouncement = groupAnnouncementRepository.findTopByGroupOrderByTimeStampDesc(group)
                        .orElseThrow(() -> new IllegalArgumentException("해당 그룹에 등록된 공지사항이 없습니다."));
                return toResponseDTO(latestGroupAnnouncement);
            } else {
                throw new IllegalArgumentException("그룹을 찾을 수 없습니다. groupName: " + groupName);
            }
        } else {
            throw new IllegalArgumentException("요청자가 해당 그룹의 멤버가 아닙니다.");
        }
    }

    // 그룹 공지 전부 조회 (최신순 정렬되어 반환됨)
    public List<GroupAnnouncementResponseDTO> getAllGroupAnnouncements(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        // 요청자가 그룹 멤버인지 검증
        if (!groupService.isContains(groupName, userIdentifier)) {
            throw new IllegalArgumentException("요청자가 해당 그룹의 멤버가 아닙니다.");
        }

        Optional<Group> groupOptional = groupRepository.findByGroupName(groupName);
        // 해당 이름의 그룹이 존재하는지 확인
        if (groupOptional.isEmpty()) {
            throw new IllegalArgumentException("해당 그룹을 찾을 수 없습니다. groupName: " + groupName);
        }

        Group group = groupOptional.get();
        List<GroupAnnouncement> announcements = groupAnnouncementRepository.findAllByGroupOrderByTimeStampDesc(group);

        // 공지가 있다면 공지(들)을 리스트로, 없다면 빈 리스트 반환
        return announcements.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // 그룹 공지 삭제
    public void deleteGroupAnnouncement(GroupAnnouncementDeleteRequestDTO request) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        String groupName = request.getGroupName();
        Long id = request.getId();

        Optional<GroupAnnouncement> groupAnnouncementOptional = groupAnnouncementRepository.findById(id);

        // 삭제하려는 공지가 존재하는지 확인
        if (groupAnnouncementOptional.isEmpty()) {
            throw new IllegalArgumentException("삭제하려는 공지를 찾을 수 없습니다. Id: " + id);
        }

        GroupAnnouncement groupAnnouncement = groupAnnouncementOptional.get();
        // 요청한 그룹이 공지의 그룹과 일치하는지 확인
        if (!groupAnnouncement.getGroup().getGroupName().equals(groupName)) {
            throw new IllegalArgumentException("삭제하려는 공지가 요청한 그룹의 공지가 아닙니다. 요청한 groupName: " + groupName + ", 삭제하려는 공지의 groupName: " + groupAnnouncement.getGroup().getGroupName());
        }

        // 요청자가 해당 그룹의 멤버인지 확인
        if (!groupService.isContains(groupName, userIdentifier)) {
            throw new IllegalArgumentException("요청자가 해당 그룹의 멤버가 아닙니다.");
        }

        // 공지 삭제
        groupAnnouncementRepository.delete(groupAnnouncement);
    }

    // ResponseDTO로 파싱
    public GroupAnnouncementResponseDTO toResponseDTO(GroupAnnouncement entity) {
        return new GroupAnnouncementResponseDTO(
                entity.getId(),
                entity.getGroup().getGroupName(),
                entity.getTitle(),
                entity.getContent(),
                entity.getTimeStamp(),
                entity.getWriterName()
        );
    }

}
