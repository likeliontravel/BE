package org.example.be.domain.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.schedule.entity.Schedule;

public interface ScheduleRepositoryCustom {
	List<Schedule> findAllByGroupsFetchJoin(List<Group> groups);

	Optional<Schedule> findNearestUpcomingByMemberId(Long memberId, LocalDateTime startOfToday);
}
