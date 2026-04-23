package org.example.be.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

	// 채팅 페이지 입장 시 최근 메시지 조회 ( 예 : 최근 20개 )
	List<ChatMessage> findTop20ByGroupOrderByCreatedTimeDesc(Group group);

	// 채팅 더 불러오기 ( 무한스크롤 ) 시 해당 메시지 바로 이전 메시지부터 최근 20개 메시지 조회
	List<ChatMessage> findTop20ByGroupAndIdLessThanOrderByCreatedTimeDesc(Group group, Long id);

	// 특정 키워드를 포함한 메시지 찾기
	List<ChatMessage> findByGroupAndContentContainingIgnoreCaseOrderByCreatedTimeDesc(Group group, String keyword);

	// 그룹별 최신 메시지 1개 조회 - ( 그룹 채팅방 목록에서 표시할 정보 )
	Optional<ChatMessage> findTop1ByGroupOrderByCreatedTimeDesc(Group group);

}
