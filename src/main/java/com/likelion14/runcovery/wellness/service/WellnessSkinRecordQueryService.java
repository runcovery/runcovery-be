package com.likelion14.runcovery.wellness.service;
import com.likelion14.runcovery.wellness.dto.SkinRecordResponseDto;
import com.likelion14.runcovery.wellness.repository.SkinRecordQueryRepository;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WellnessSkinRecordQueryService {

    private final UserRepository userRepository;
    private final SkinRecordQueryRepository skinRecordQueryRepository;

    public List<SkinRecordResponseDto> getRecords(Long userId, LocalDate measuredDate) {
        if (userId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId는 필수입니다.");
        }
        if (!userRepository.existsById(userId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        LocalDate targetDate = measuredDate == null ? LocalDate.now() : measuredDate;
        return skinRecordQueryRepository
                .findAllByUser_IdAndMeasuredDateOrderByIdAsc(userId, targetDate)
                .stream()
                .map(SkinRecordResponseDto::from)
                .toList();
    }
}

