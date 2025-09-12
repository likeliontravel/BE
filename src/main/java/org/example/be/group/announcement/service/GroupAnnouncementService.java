package org.example.be.group.announcement.service;

import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
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
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
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
    @Transactional
    public GroupAnnouncementResponseDTO createGroupAnnouncement(GroupAnnouncementCreationRequestDTO dto) {
        // 저장할 정보 가져오기
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        String rawGroupName = dto.getGroupName();
        String groupName = needsDecoding(rawGroupName) ? URLDecoder.decode(rawGroupName, StandardCharsets.UTF_8) : rawGroupName;

        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. groupName: " + groupName));

        if (!groupService.isContains(groupName, userIdentifier)) {
            throw new ForbiddenResourceAccessException("요청자가 해당 그룹의 멤버가 아닙니다.");
        }

        GroupAnnouncement newAnnouncement = new GroupAnnouncement();
        newAnnouncement.setGroup(group);
        newAnnouncement.setTitle(dto.getTitle());
        newAnnouncement.setContent(dto.getContent());
        newAnnouncement.setTimeStamp(LocalDateTime.now());
        newAnnouncement.setWriterName(unifiedUserService.getNameByUserIdentifier(userIdentifier));

        return toResponseDTO(groupAnnouncementRepository.save(newAnnouncement));
    }

    // 최상단 노출 그룹 공지 1개만 조회
    @Transactional(readOnly = true)
    public GroupAnnouncementResponseDTO getLatestAnnouncement(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. groupName: " + groupName));

        if (!groupService.isContains(groupName, userIdentifier)) {
            throw new ForbiddenResourceAccessException("요청자가 해당 그룹의 멤버가 아닙니다.");
        }

        Optional<GroupAnnouncement> latestAnnouncementOptional = groupAnnouncementRepository.findTopByGroupOrderByTimeStampDesc(group);

        if (latestAnnouncementOptional.isEmpty()) {
            throw new NoSuchElementException("해당 그룹에 등록된 공지가 없습니다.");
        }

        return toResponseDTO(latestAnnouncementOptional.get());
    }

    // 그룹 공지 전부 조회 (최신순 정렬되어 반환됨)
    @Transactional(readOnly = true)
    public List<GroupAnnouncementResponseDTO> getAllGroupAnnouncements(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();


        // 요청자가 그룹 멤버인지 검증
        if (!groupService.isContains(groupName, userIdentifier)) {
            throw new ForbiddenResourceAccessException("요청자가 해당 그룹의 멤버가 아닙니다.");
        }

        Optional<Group> groupOptional = groupRepository.findByGroupName(groupName);
        // 해당 이름의 그룹이 존재하는지 확인
        if (groupOptional.isEmpty()) {
            throw new NoSuchElementException("해당 그룹을 찾을 수 없습니다. groupName: " + groupName);
        }

        Group group = groupOptional.get();
        List<GroupAnnouncement> announcements = groupAnnouncementRepository.findAllByGroupOrderByTimeStampDesc(group);

        // 공지가 있다면 공지(들)을 리스트로, 없다면 빈 리스트 반환
        return announcements.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // 그룹 공지 삭제
    @Transactional
    public void deleteGroupAnnouncement(GroupAnnouncementDeleteRequestDTO request) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        String rawGroupName = request.getGroupName();
        String groupName = needsDecoding(rawGroupName) ? URLDecoder.decode(rawGroupName, StandardCharsets.UTF_8) : rawGroupName;

        GroupAnnouncement groupAnnouncement = groupAnnouncementRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 공지사항을 찾을 수 없습니다. id: " + request.getId()));

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

    // 문자열이 URL 인코딩 상태인지 검사. 디코딩이 필요한 경우 true 반환
    private boolean needsDecoding(String s) {
        return s != null && s.contains("%");
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
