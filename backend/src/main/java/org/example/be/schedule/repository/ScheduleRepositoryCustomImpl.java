package org.example.be.schedule.repository;

import java.util.List;

import org.example.be.group.entitiy.Group;
import org.example.be.schedule.entity.QSchedule;
import org.example.be.schedule.entity.QSchedulePlace;
import org.example.be.schedule.entity.Schedule;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScheduleRepositoryCustomImpl implements ScheduleRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Schedule> findAllByGroupsFetchJoin(List<Group> groups) {
		QSchedule schedule = QSchedule.schedule;
		QSchedulePlace schedulePlace = QSchedulePlace.schedulePlace;

		return queryFactory
			.selectFrom(schedule)
			.leftJoin(schedule.group).fetchJoin()
			.leftJoin(schedule.schedulePlaces, schedulePlace).fetchJoin()
			.where(schedule.group.in(groups))
			.distinct() // OneToMany 조인 시 중복 데이터 제거
			.fetch();
	}
}
