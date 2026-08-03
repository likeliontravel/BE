package org.example.be.domain.group.announcement.repository;

import org.example.be.domain.group.announcement.entity.GroupAnnouncement;
import org.example.be.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupAnnouncementRepository extends JpaRepository<GroupAnnouncement, Long> {
    // 가장 최신 공지 1개 조회
    Optional<GroupAnnouncement> findTopByGroupOrderByTimeStampDesc(Group group);
    // 해당 그룹의 공지 최신순으로 전부 조회
    List<GroupAnnouncement> findAllByGroupOrderByTimeStampDesc(Group group);

    // 그룹 삭제 시 해당 그룹의 공지 전부 삭제
    @Modifying
    @Query("DELETE FROM GroupAnnouncement ga WHERE ga.group = :group")
    void deleteByGroup(@Param("group") Group group);
}
