package org.example.be.group.repository;

import org.example.be.group.entitiy.Group;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String groupName);

    List<Group> findByMembersContaining(UnifiedUser user);

    // ( 위 findByGroupName은 기본 LAZY 로딩 전략이라 서비스단 트랜잭션 밖에서 그룹 정보를 호출하면
    // 실제 DB 쿼리가 나가지 않고 프록시 객체에만 접근할 수 있기 때문에 밖에서 사용할 수 있는 쿼리를 따로 판겁니다.)
    // 채팅 ChannelInterceptor, 그룹 상세조회에서 사용할 fetch join 쿼리
    // 그룹과 함께 그에 속한 멤버들까지 로딩
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.groupName = :groupName")
    Optional<Group> findWithMembersByGroupName(@Param("groupName") String groupName);

}
