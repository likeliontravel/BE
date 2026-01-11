package org.example.be.group.invitation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.group.entitiy.Group;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_invitation")
public class GroupInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 랜덤 UUID 기반 초대 코드 (그룹 정보가 노출되지 않게 하기 위함)
    @Column(nullable = false, unique = true)
    private String invitationCode;

    @ManyToOne
    @JoinColumn(name = "user_groups_id", nullable = false)
    private Group group;

    // 초대 링크 생성 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 생성 시각 기준 6시간 이후 만료
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 활성화 여부 ( 새 링크 생성 시 해당 그룹의 기존 초대링크 무효화용 )
    @Column(nullable = false)
    private boolean active = true;
}
