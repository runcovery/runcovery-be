package com.likelion14.runcovery.wellness;

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

    public List<SkinRecordResponseDto> getRecords(Long memberId, LocalDate measuredDate) {
        if (memberId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "memberId는 필수입니다.");
        }
        if (!userRepository.existsById(memberId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        LocalDate targetDate = measuredDate == null ? LocalDate.now() : measuredDate;
        return skinRecordQueryRepository
                .findAllByUser_IdAndMeasuredDateOrderByIdAsc(memberId, targetDate)
                .stream()
                .map(SkinRecordResponseDto::from)
                .toList();
    }
}
