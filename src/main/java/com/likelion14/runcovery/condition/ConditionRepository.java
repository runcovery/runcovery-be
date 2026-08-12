package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ConditionRepository extends JpaRepository<TodayCondition, Long> {
    Optional<TodayCondition> findByUserAndConditionDate(User user, LocalDate conditionDate);
}
