package org.example.be.domain.schedule.repository;

import org.example.be.domain.schedule.entity.Schedule;
import org.example.be.domain.schedule.entity.SchedulePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulePlaceRepository extends JpaRepository<SchedulePlace, Long> {

    List<SchedulePlace> findBySchedule(Schedule schedule);
}