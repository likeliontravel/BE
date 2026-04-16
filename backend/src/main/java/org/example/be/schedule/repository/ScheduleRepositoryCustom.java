package org.example.be.schedule.repository;

import java.util.List;

import org.example.be.group.entitiy.Group;
import org.example.be.schedule.entity.Schedule;

public interface ScheduleRepositoryCustom {
	List<Schedule> findAllByGroupsFetchJoin(List<Group> groups);
}
