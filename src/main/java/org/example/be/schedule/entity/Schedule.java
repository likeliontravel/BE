package org.example.be.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.be.group.entitiy.Group;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule")

public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일정 시작 시각
    @Column(nullable = false)
    private LocalDateTime startSchedule;

    // 일정 종료 시각
    @Column(nullable = false)
    private LocalDateTime endSchedule;

    //일정이 속한 그룹의 ID
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // 해당 일정에 포함된 장소들(식당이나 숙소 관광지들)
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchedulePlace> schedulePlaces = new ArrayList<>();

}
