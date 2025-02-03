package org.example.be.group.invitation.service;

import lombok.RequiredArgsConstructor;
import org.example.be.group.entitiy.Group;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.repository.GroupInvitationRepository;
import org.example.be.group.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupInvitationService {

    private final GroupInvitationRepository invitationRepository;
    private final GroupRepository groupRepository;
    private static final long INVITATION_VALID_HOURS = 6; // 초대코드가 만료될 시간 6으로 초기화

    // 그룹 초대 링크 생성 메서드
    @Transactional
    public GroupInvitation generateInvitation(String groupName, String userIdentifier, boolean generateNew) {
        // 그룹을 조회하고, 요청한 사용자가 해당 그룹의 창설자(creator)인지 검증.
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다. groupName: " + groupName));

        if (!group.getCreatedBy().getUserIdentifier().equals(userIdentifier)) {
            throw new IllegalArgumentException("초대 링크는 그룹 창설자만 생성할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        // 해당 그룹에 대해 현재 유효한 초대 링크가 있는지 찾음.
        Optional<GroupInvitation> existingOpt = invitationRepository.findByGroupAndActiveTrueAndExpiresAtAfter(group, now);
        if (existingOpt.isPresent()) {  // 유효한 링크가 있다면 해당 링크 정보를 바로 반환해 준다.
            if (!generateNew) {     // 새로 생성하라는 요청이 아니었다면 기존 링크 정보를 반환해준다.
                return existingOpt.get();
            } else {    // 새로 생성하라는 요청이었다면 기존 링크를 무효화하고 아래 생성 로직을 따른다.
                GroupInvitation existing = existingOpt.get();
                existing.setActive(false);  // 무효화(활성화 여부를 false로 바꾼다. 이후 사용할 수 없게 된다.
                invitationRepository.save(existing);    // 해당 링크 활성화여부 변경사항 저장
            }
        }

        // ===================== 새 초대 링크 생성 =====================
        GroupInvitation invitation = new GroupInvitation();
        // 기존 UUID의 '-' 제거
        invitation.setInvitationCode(UUID.randomUUID().toString().replace("-",""));
        // 입력된 그룹에 대한 링크로 설정
        invitation.setGroup(group);
        // 현재 시각을 링크 생성시각으로 설정
        invitation.setCreatedAt(now);
        // 만료 시각을 현재 시각으로부터 INVITATION_VALID_HOURS 만큼 지난 시각으로 설정
        invitation.setExpiresAt(now.plusHours(INVITATION_VALID_HOURS)); // plusHours() : 입력된 시간(시) 단위 +
        invitation.setActive(true); // 생성된 링크를 활성화 설정
        return invitationRepository.save(invitation);   // 생성된 링크 DB에 저장 ( 동시에 해당 링크 호출부로 반환 )
    }

    // 초대 코드를 받아 해당 그룹 초대링크객체를 반환해주는 메서드
    @Transactional(readOnly = true)
    public GroupInvitation getValidInvitation(String invitationCode) {
        LocalDateTime now = LocalDateTime.now();
        return invitationRepository.findByInvitationCodeAndActiveTrueAndExpiresAtAfter(invitationCode, now)
                .orElseThrow(() -> new IllegalArgumentException("초대 링크가 유효하지 않거나 만료되었습니다. 초대 코드: " + invitationCode));
    }

}
