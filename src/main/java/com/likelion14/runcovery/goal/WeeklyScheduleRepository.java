package com.likelion14.runcovery.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {
    List<WeeklySchedule> findByWeeklyGoal(WeeklyGoal weeklyGoal);
}
