package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ConditionRepository extends JpaRepository<TodayCondition, Long> {
    Optional<TodayCondition> findByUserAndConditionDate(User user, LocalDate conditionDate);

    // 가장 최근 컨디션 조회
    Optional<TodayCondition> findFirstByUserOrderByConditionDateDesc(User user);

    // 이번주 생성된 컨디션 수
    int countByUserAndConditionDateBetween(User user, LocalDate start, LocalDate end);

    // 이번주 체크 완료된 컨디션 수
    int countByUserAndConditionDateBetweenAndIsCheckedTrue(User user, LocalDate start, LocalDate end);

}
