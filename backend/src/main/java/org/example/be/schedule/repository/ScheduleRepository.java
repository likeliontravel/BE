package org.example.be.schedule.repository;

import org.example.be.group.entitiy.Group;
import org.example.be.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Optional<Schedule> findByGroup(Group group);
}
