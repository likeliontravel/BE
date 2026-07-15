package org.example.be.domain.schedule.repository;

import java.util.Optional;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {
	Optional<Schedule> findByGroup(Group group);
}
