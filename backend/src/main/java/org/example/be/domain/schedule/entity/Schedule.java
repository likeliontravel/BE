package org.example.be.domain.schedule.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.be.domain.group.entity.Group;
import org.example.be.global.entity.Base;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedule")

public class Schedule extends Base {

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

	public static Schedule create(LocalDateTime startSchedule, LocalDateTime endSchedule, Group group) {
		Schedule schedule = new Schedule();
		schedule.startSchedule = startSchedule;
		schedule.endSchedule = endSchedule;
		schedule.group = group;
		return schedule;
	}

	public void update(LocalDateTime startSchedule, LocalDateTime endSchedule, Group group) {
		this.startSchedule = startSchedule;
		this.endSchedule = endSchedule;
		this.group = group;
	}

	public void addSchedulePlace(SchedulePlace schedulePlace) {
		this.schedulePlaces.add(schedulePlace);
		schedulePlace.assignSchedule(this);
	}
}
