package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyGoalRepository extends JpaRepository<WeeklyGoal, Long> {
    Optional<WeeklyGoal> findTopByUserOrderByWeekNoDesc(User user);
}
