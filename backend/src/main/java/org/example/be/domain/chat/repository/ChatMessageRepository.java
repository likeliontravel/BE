package org.example.be.domain.chat.repository;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

	@Modifying
	@Query("DELETE FROM ChatMessage c WHERE c.group = :group")
	void deleteByGroup(@Param("groupId") Group group);

	@Modifying
	@Query("DELETE From ChatMessage c WHERE c.sender = :sender")
	void deleteBySender(@Param("sender") String sender);
}
