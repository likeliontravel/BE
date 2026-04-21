package org.example.be.domain.chat.entity;

import org.example.be.domain.chat.type.MessageType;
import org.example.be.domain.group.entity.Group;
import org.example.be.domain.member.entity.Member;
import org.example.be.global.entity.Base;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chat_message")
public class ChatMessage extends Base {

	// 그룹 정보
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	// 송신자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private Member sender;

	// 메시지 유형 - 알맞은 enum(하단에 정의)을 db에 기록하기 위한 컬럼
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageType type;

	// 전달 내용 - 메시지 내용 / 이미지 publicUrl
	@Column(columnDefinition = "TEXT")
	private String content;

	public static ChatMessage create(Group group, Member sender, MessageType type, String content) {
		return new ChatMessage(group, sender, type, content);
	}

}
