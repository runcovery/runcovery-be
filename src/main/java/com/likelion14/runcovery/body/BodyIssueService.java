package com.likelion14.runcovery.body;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        List<PainAreaDto> painAreas = bodyIssueRepository.findAllByUser_IdAndIsPainfulTrue(userId).stream()
                .map(PainAreaDto::new)
                .toList();
        return new BodyIssueListResponseDto(painAreas);
    }

    @Transactional
    public BodyIssueSaveResponseDto saveBodyIssues(Long userId, BodyIssueSaveRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        int updatedCount = 0;
        for (PainAreaDto painArea : request.getPainAreas()) {
            BodyIssueId id = new BodyIssueId(userId, painArea.getBodyPartCode());
            BodyIssue existing = bodyIssueRepository.findById(id).orElse(null);

            if (existing == null) {
                BodyPart bodyPart = bodyPartRepository.findById(painArea.getBodyPartCode())
                        .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 신체 부위가 존재하지 않습니다"));
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
