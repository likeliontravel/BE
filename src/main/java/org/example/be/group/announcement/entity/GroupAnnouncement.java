package org.example.be.group.announcement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.group.entitiy.Group;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_groups_id", nullable = false)
    private Group group;

    @Column
    private String writerName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timeStamp;
}
