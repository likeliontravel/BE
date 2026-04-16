package org.example.be.domain.schedule.entity;

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule_place")

public class SchedulePlace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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

}
