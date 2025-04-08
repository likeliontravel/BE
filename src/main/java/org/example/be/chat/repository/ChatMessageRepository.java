package org.example.be.chat.repository;

import org.example.be.chat.entity.ChatMessage;
import org.example.be.group.entitiy.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 입장 시 최근 메시지 조회 ( 예 : 최근 100개 )
    List<ChatMessage> findTop100ByGroupOrderBySendAtDesc(Group group);

    // 특정 키워드를 포함한 메시지 찾기
    List<ChatMessage> findByGroupAndContentContainingIgnoreCaseOrderBySendAtDesc(Group group, String keyword);

    // 그룹별 최신 메시지 1개 조회 - ( 그룹 채팅방 목록에서 표시할 정보 )
    ChatMessage findTop1ByGroupOrderBySendAtDesc(Group group);
}
