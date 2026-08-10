package com.likelion14.runcovery.service;

import com.likelion14.runcovery.dto.UserCreateRequestDto;
import com.likelion14.runcovery.dto.UserCreateResponseDto;
import com.likelion14.runcovery.dto.UserResponseDto;
import com.likelion14.runcovery.entity.User;
import com.likelion14.runcovery.exception.UserNotFoundException;
import com.likelion14.runcovery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserCreateResponseDto createUser(UserCreateRequestDto request) {
        User user = new User(
                request.getNickname(),
                request.getAge(),
                request.getGender(),
                request.getHeight(),
                request.getWeight(),
                request.getRunningExperience(),
                request.getMaxRunDuration(),
                request.getAvgSleepHours()
        );
        User savedUser = userRepository.save(user);
        return new UserCreateResponseDto(savedUser);
    }

    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return new UserResponseDto(user);
    }
}
