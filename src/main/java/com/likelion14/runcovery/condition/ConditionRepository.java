package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ConditionRepository extends JpaRepository<Condition, Long> {

    // 유저와 날짜로 해당 날짜의 컨디션 조회
    Optional<Condition> findByUserAndConditionDate(User user, LocalDate conditionDate);

    /**
     * 활동 날짜와 같은 컨디션 중 가장 최근에 생성된(condition_id가 가장 큰) 기록을 반환합니다.
     */
    Optional<Condition> findFirstByUserAndConditionDateOrderByIdDesc(User user, LocalDate conditionDate);

    // 가장 최근 컨디션 조회
    Optional<Condition> findFirstByUserOrderByConditionDateDesc(User user);

    // 이번주 생성된 컨디션 수
    int countByUserAndConditionDateBetween(User user, LocalDate start, LocalDate end);

    // 이번주 체크 완료된 컨디션 수
    int countByUserAndConditionDateBetweenAndIsCheckedTrue(User user, LocalDate start, LocalDate end);

}
