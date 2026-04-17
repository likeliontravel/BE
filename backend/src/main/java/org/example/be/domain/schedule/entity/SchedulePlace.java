package org.example.be.domain.schedule.entity;

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.global.entity.Base;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedule_place")

public class SchedulePlace extends Base {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "schedule_id", nullable = false)
	private Schedule schedule;

	@Column(nullable = false)
	private String contentId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlaceType placeType;

	// 방문 시작 시간
	@Column(nullable = false)
	private LocalDateTime visitStart;

	// 방문 종료 시간
	@Column(nullable = false)
	private LocalDateTime visitedEnd;

	// 몇 일차 일정인지(예를 들어 1일차, 2일차, 3일차)
	@Column(nullable = false)
	private Integer dayOrder;

	// dayOrder에서 몇 번째 일정인지(1일차의 1번째 방문 장소)
	@Column(nullable = false)
	private Integer orderInDay;

	public static SchedulePlace create(Schedule schedule, String contentId, PlaceType placeType,
		LocalDateTime visitStart, LocalDateTime visitedEnd, Integer dayOrder, Integer orderInDay) {
		SchedulePlace place = new SchedulePlace();
		place.contentId = contentId;
		place.placeType = placeType;
		place.visitStart = visitStart;
		place.visitedEnd = visitedEnd;
		place.dayOrder = dayOrder;
		place.orderInDay = orderInDay;

		schedule.addSchedulePlace(place);
		return place;
	}

	public void update(String contentId, PlaceType placeType, LocalDateTime visitStart, LocalDateTime visitedEnd,
		Integer dayOrder, Integer orderInDay) {
		this.contentId = contentId;
		this.placeType = placeType;
		this.visitStart = visitStart;
		this.visitedEnd = visitedEnd;
		this.dayOrder = dayOrder;
		this.orderInDay = orderInDay;
	}

	public void assignSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
}
