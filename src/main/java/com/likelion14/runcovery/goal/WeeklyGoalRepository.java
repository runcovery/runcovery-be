package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WeeklyGoalRepository extends JpaRepository<WeeklyGoal, Long> {

    Optional<WeeklyGoal> findTopByFutureGoalOrderByWeekNoDesc(FutureGoal futureGoal);

    Optional<WeeklyGoal> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

}
