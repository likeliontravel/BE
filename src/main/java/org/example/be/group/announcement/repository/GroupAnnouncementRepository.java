package org.example.be.group.announcement.repository;

import org.example.be.group.announcement.entity.GroupAnnouncement;
import org.example.be.group.entitiy.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupAnnouncementRepository extends JpaRepository<GroupAnnouncement, Long> {
    // 가장 최신 공지 1개 조회
    Optional<GroupAnnouncement> findTopByGroupOrderByTimeStampDesc(Group group);
    // 해당 그룹의 공지 최신순으로 전부 조회
    List<GroupAnnouncement> findAllByGroupOrderByTimeStampDesc(Group group);
}
