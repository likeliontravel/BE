package org.example.be.domain.chat.repository;

import java.util.List;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.group.entity.Group;

public interface ChatMessageRepositoryCustom {
	List<ChatMessage> findLatestMessagesForGroups(List<Group> groups);
}
