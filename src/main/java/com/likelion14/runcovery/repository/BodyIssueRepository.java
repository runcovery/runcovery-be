package com.likelion14.runcovery.repository;

import com.likelion14.runcovery.entity.BodyIssue;
import com.likelion14.runcovery.entity.BodyIssueId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BodyIssueRepository extends JpaRepository<BodyIssue, BodyIssueId> {
    List<BodyIssue> findAllByUser_IdAndIsPainfulTrue(Long userId);
}
