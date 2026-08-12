package com.likelion14.runcovery.condition;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<TodayCondition, Long> {
}
