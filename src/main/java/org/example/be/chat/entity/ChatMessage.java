package org.example.be.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.be.group.entitiy.Group;
import org.example.be.unifieduser.entity.UnifiedUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 그룹 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // 송신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UnifiedUser sender;

    // 메시지 유형 - 알맞은 enum(하단에 정의)을 db에 기록하기 위한 컬럼
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    // 전달 내용 - 메시지 내용 / 이미지 publicUrl
    @Column(columnDefinition = "TEXT")
    private String content;

    // 메시지 전송 시간
    @Column(nullable = false)
    private LocalDateTime sendAt;

    // 메시지 유형 ( 추후 확장 고려 enum )
    public enum MessageType {
        TEXT, IMAGE
    }
}
