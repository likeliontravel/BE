package org.example.be.chat.repository;

import org.example.be.chat.entity.ChatMessage;
import org.example.be.group.entitiy.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅 페이지 입장 시 최근 메시지 조회 ( 예 : 최근 20개 )
    List<ChatMessage> findTop20ByGroupOrderBySendAtDesc(Group group);

    // 채팅 더 불러오기 ( 무한스크롤 ) 시 해당 메시지 바로 이전 메시지부터 최근 20개 메시지 조회
    List<ChatMessage> findTop20ByGroupAndIdLessThanOrderBySendAtDesc(Group group, Long id);

    // 특정 키워드를 포함한 메시지 찾기
    List<ChatMessage> findByGroupAndContentContainingIgnoreCaseOrderBySendAtDesc(Group group, String keyword);

    // 그룹별 최신 메시지 1개 조회 - ( 그룹 채팅방 목록에서 표시할 정보 )
    Optional<ChatMessage> findTop1ByGroupOrderBySendAtDesc(Group group);

    // 여러 그룹에 대해 각 그룹의 가장 최근 메시지 전부 가져오기
    // - 각 그룹 g에 대해 sendAt 최대값을 가진 메시지를 가져온다
    // - 동시간대 동률이 있을 수 있으므로 서비스에서 최종 1개로 정리
    @Query("""
           SELECT m
             FROM ChatMessage m
             JOIN m.group g
            WHERE g IN :groups
              AND m.sendAt = (
                    SELECT MAX(m2.sendAt)
                      FROM ChatMessage m2
                     WHERE m2.group = g
              )
           """)
    List<ChatMessage> findLatestMessagesForGroups(@Param("groups") List<Group> groups);

}
