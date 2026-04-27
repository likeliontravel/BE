package org.example.be.domain.chat.repository;

import java.util.Optional;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.group.entity.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

	// 그룹별 최신 메시지 1개 조회 - ( 그룹 채팅방 목록에서 표시할 정보 )
	@EntityGraph(attributePaths = {"group", "sender"})
	Optional<ChatMessage> findTop1ByGroupOrderByCreatedTimeDesc(Group group);

}
