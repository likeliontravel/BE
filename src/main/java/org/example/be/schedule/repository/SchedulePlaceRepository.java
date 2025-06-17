package org.example.be.schedule.repository;

import org.example.be.schedule.entity.SchedulePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulePlaceRepository extends JpaRepository<SchedulePlace, Long> {

}