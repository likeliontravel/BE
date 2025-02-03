package org.example.be.group.invitation.repository;

import org.example.be.group.entitiy.Group;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    // 해당 그룹에 대해; 활성 상태이고 만료 시각이 현재 시각보다 이후인 (아직 유효한) 초대링크 찾기용
    Optional<GroupInvitation> findByGroupAndActiveTrueAndExpiresAtAfter(Group group, LocalDateTime now);

    // 해당 초대코드에 대해; 활성 상태이고, 만료 시각이 현재 시각보다 이후인 (아직 유효한) 초대링크 찾기용
    Optional<GroupInvitation> findByInvitationCodeAndActiveTrueAndExpiresAtAfter(String invitationCode, LocalDateTime now);
}
