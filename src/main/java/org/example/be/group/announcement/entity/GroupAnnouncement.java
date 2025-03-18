package org.example.be.group.announcement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.group.entitiy.Group;
import org.example.be.unifieduser.entity.UnifiedUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "group_announcement")
@NoArgsConstructor
public class GroupAnnouncement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_groups_id", nullable = false)
    private Group group;

    @Column
    private String writerName;

    @Column(nullable = false)
    private String title;

    @Column
    private String content;

    @Column(nullable = false)
    private LocalDateTime timeStamp;
}
