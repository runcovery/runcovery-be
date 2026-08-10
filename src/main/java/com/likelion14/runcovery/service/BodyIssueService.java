package com.likelion14.runcovery.service;

import com.likelion14.runcovery.dto.BodyIssueListResponseDto;
import com.likelion14.runcovery.dto.BodyIssueSaveRequestDto;
import com.likelion14.runcovery.dto.BodyIssueSaveResponseDto;
import com.likelion14.runcovery.dto.PainAreaDto;
import com.likelion14.runcovery.entity.BodyIssue;
import com.likelion14.runcovery.entity.BodyIssueId;
import com.likelion14.runcovery.entity.BodyPart;
import com.likelion14.runcovery.entity.User;
import com.likelion14.runcovery.exception.BodyPartNotFoundException;
import com.likelion14.runcovery.exception.UserNotFoundException;
import com.likelion14.runcovery.repository.BodyIssueRepository;
import com.likelion14.runcovery.repository.BodyPartRepository;
import com.likelion14.runcovery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BodyIssueService {

    private final BodyIssueRepository bodyIssueRepository;
    private final BodyPartRepository bodyPartRepository;
    private final UserRepository userRepository;

    public BodyIssueListResponseDto getBodyIssues(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<PainAreaDto> painAreas = bodyIssueRepository.findAllByUser_IdAndIsPainfulTrue(userId).stream()
                .map(PainAreaDto::new)
                .toList();
        return new BodyIssueListResponseDto(painAreas);
    }

    @Transactional
    public BodyIssueSaveResponseDto saveBodyIssues(Long userId, BodyIssueSaveRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedCount = 0;
        for (PainAreaDto painArea : request.getPainAreas()) {
            BodyIssueId id = new BodyIssueId(userId, painArea.getBodyPartCode());
            BodyIssue existing = bodyIssueRepository.findById(id).orElse(null);

            if (existing == null) {
                BodyPart bodyPart = bodyPartRepository.findById(painArea.getBodyPartCode())
                        .orElseThrow(BodyPartNotFoundException::new);
                bodyIssueRepository.save(new BodyIssue(user, bodyPart, painArea.getIsPainful()));
                updatedCount++;
            } else if (!existing.getIsPainful().equals(painArea.getIsPainful())) {
                existing.update(painArea.getIsPainful());
                updatedCount++;
            }
        }

        return new BodyIssueSaveResponseDto(true, updatedCount);
    }
}
