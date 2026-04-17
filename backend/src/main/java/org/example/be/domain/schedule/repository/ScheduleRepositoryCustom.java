package org.example.be.domain.schedule.repository;

import java.util.List;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.schedule.entity.Schedule;

public interface ScheduleRepositoryCustom {
	List<Schedule> findAllByGroupsFetchJoin(List<Group> groups);
}
