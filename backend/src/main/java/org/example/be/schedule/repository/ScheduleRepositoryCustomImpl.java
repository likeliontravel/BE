package org.example.be.schedule.repository;

import static org.example.be.group.entitiy.QGroup.*;
import static org.example.be.schedule.entity.QSchedule.*;
import static org.example.be.schedule.entity.QSchedulePlace.*;

import java.util.List;

import org.example.be.group.entitiy.Group;
import org.example.be.schedule.entity.Schedule;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScheduleRepositoryCustomImpl implements ScheduleRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Schedule> findAllByGroupsFetchJoin(List<Group> groups) {
		return queryFactory
			.selectFrom(schedule)
			.leftJoin(schedule.group, group).fetchJoin()
			.leftJoin(schedule.schedulePlaces, schedulePlace).fetchJoin()
			.where(schedule.group.in(groups))
			.distinct()
			.fetch();
	}
}
