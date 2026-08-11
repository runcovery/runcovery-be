package com.likelion14.runcovery.body;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BodyIssueRepository extends JpaRepository<BodyIssue, BodyIssueId> {
    List<BodyIssue> findAllByUser_IdAndIsPainfulTrue(Long userId);
}
