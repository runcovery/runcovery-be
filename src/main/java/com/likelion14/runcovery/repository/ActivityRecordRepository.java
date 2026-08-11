package com.likelion14.runcovery.repository;

import com.likelion14.runcovery.entity.ActivityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {
}
