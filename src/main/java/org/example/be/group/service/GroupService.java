package org.example.be.group.service;

import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.*;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UnifiedUserRepository unifiedUserRepository;

    // 그룹 생성하기
    public GroupResponseDTO createGroup(GroupCreationRequestDTO request) {

        String createdBy = request.getCreatedBy();

        UnifiedUser creator = unifiedUserRepository.findByUserIdentifier(createdBy)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. userIdentifier : " + createdBy));

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.addMember(creator);

        Group groupBeforeConvertTODTO = groupRepository.save(group);

        return toResponseDTO(groupBeforeConvertTODTO);
    }


    // 그룹에 멤버 추가 (그룹 초대 구현 후 수정될 수 있음)
    public void addMemberToGroup(GroupAddMemberRequestDTO request) {
        String groupName = request.getGroupName();
        String userIdentifier = request.getUserIdentifier();

        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName : " + groupName));

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 userIdentifier의 유저를 찾을 수 없습니다. userIdnetifier : " + userIdentifier));

        group.addMember(user);
        groupRepository.save(group);
    }

    // 그룹 설명 또는 공지사항 변경
    public void modifyDescribtionOrAnnouncement(GroupModifyRequestDTO request) {

        String groupName = request.getGroupName();
        String userIdentifier = request.getUserIdentifier();    // 요청자의 userIdentifier
        String description = request.getDescription();
        String announcement = request.getAnnouncement();

        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName : " + groupName));

        // 변경 전 요청을 보낸 사람이 그룹 내 멤버가 맞는지 확인하기
        if (isContains(groupName, userIdentifier)) {
            if (description != null) {
                group.setDescription(description);
            }
            if (announcement != null) {
                group.setAnnouncement(announcement);
            }
            groupRepository.save(group);
        } else {
            throw new IllegalArgumentException("이 그룹에 포함되어 있지 않은 멤버입니다. userIdentifier : " + userIdentifier + ", groupName : " + groupName);
        }
    }

    // 그룹 나가기
    public void exitGroup(GroupExitOrDeleteRequestDTO request) {
        String groupName = request.getGroupName();
        String requesterUserIdentifier = request.getUserIdentifier();

        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName : " + groupName));

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(requesterUserIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier : " + requesterUserIdentifier));

        if (isContains(groupName, requesterUserIdentifier)) {
            if (!requesterUserIdentifier.equals(group.getCreatedBy().getUserIdentifier())){
                group.removeMember(user);
            } else {
                throw new IllegalArgumentException("그룹의 창설자는 그룹을 나갈 수 없습니다. 그룹을 삭제하세요.");
            }

        } else {
            throw new NoSuchElementException("이 그룹에 포함되어 있지 않은 멤버입니다. groupName : " + groupName + ", userIdentifier : " + requesterUserIdentifier);
        }
    }

    // 유저가 가입한 그룹 정보 조회하기
    public List<GroupResponseDTO> getAllGroups(String userIdentifier) {
        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        List<Group> groups = groupRepository.findByMembersContaining(user)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 가입한 그룹을 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        List<GroupResponseDTO> groupDTOList;
        if(!groups.isEmpty()) {
            groupDTOList = groups.stream()
                    .map(group -> new GroupResponseDTO(group.getId(), group.getGroupName(), group.getDescription(), group.getCreatedBy().getUserIdentifier()))
                    .toList();
        } else {
            groupDTOList = null;
        }

        return groupDTOList;
    }

    // 그룹 삭제하기 (그룹 생성한 유저만 가능)
    public void deleteGroup(GroupExitOrDeleteRequestDTO request) {

        String groupName = request.getGroupName();
        String userIdentifier = request.getUserIdentifier();

        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName : " + groupName));

        if (isContains(groupName, userIdentifier)){
            if (userIdentifier.equals(group.getCreatedBy().getUserIdentifier())) {  // 그룹의 멤버이면 그룹을 만든 사람인지 검증
                System.out.println("그룹을 삭제합니다. groupName : " + groupName);
                // 그룹을 만든 사람이면 그룹 삭제.
                groupRepository.delete(group);

                // 이전 그룹 채팅 메시지도 삭제하도록 호출 (추후 채팅때 구현)


            } else {
                throw new IllegalArgumentException("그룹 창설자만 그룹을 삭제할 수 있습니다.");
            }
        } else {
            throw new IllegalArgumentException("요청한 사용자가 그룹의 멤버가 아닙니다. groupName : " + groupName + ", userIdentifier : " + userIdentifier);
        }

    }

    // 이 그룹에 해당 멤버가 있는지 조회하기
    private Boolean isContains(String groupName, String userIdentifier) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName : " + groupName));

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        if (group.getMembers().contains(user)) {
            return true;
        } else {
            return false;
        }
    }

    // converter to responseDTO
    private GroupResponseDTO toResponseDTO(Group group) {
        return new GroupResponseDTO(group.getId(), group.getGroupName(), group.getDescription(), group.getCreatedBy().getUserIdentifier());
    }

}
