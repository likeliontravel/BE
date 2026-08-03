package org.example.be.domain.group.invitation.repository;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.invitation.entity.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    // 해당 그룹에 대해; 활성 상태이고 만료 시각이 현재 시각보다 이후인 (아직 유효한) 초대링크 찾기용
    Optional<GroupInvitation> findByGroupAndActiveTrueAndExpiresAtAfter(Group group, LocalDateTime now);

    // 그룹 삭제 시 해당 그룹의 초대 링크 전부 삭제
    @Modifying
    @Query("DELETE FROM GroupInvitation gi WHERE gi.group = :group")
    void deleteByGroup(@Param("group") Group group);

//    // 해당 초대코드에 대해; 활성 상태이고, 만료 시각이 현재 시각보다 이후인 (아직 유효한) 초대링크 찾기용
//    Optional<GroupInvitation> findByInvitationCodeAndActiveTrueAndExpiresAtAfter(String invitationCode, LocalDateTime now);

    // 해당 그룹에 대해 활성 상태인 초대 링크 찾기
    Optional<GroupInvitation> findByGroupAndActiveTrue(Group group);

    // 해당 그룹에 대해 활성 상태이지만 유효 기간이 지나지 않은 링크 찾기
    Optional<GroupInvitation> findByInvitationCodeAndActiveTrueAndExpiresAtAfter(String invitationCode, LocalDateTime now);
}
