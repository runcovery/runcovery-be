package com.likelion14.runcovery.user;

import com.likelion14.runcovery.common.AppConstants;
import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 유저 등록
    @PostMapping
    public ResponseEntity<ApiResponse<UserCreateResponseDto>> createUser(
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        UserCreateResponseDto response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    // 유저 조회
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo() {
        return ApiResponse.ok(userService.getMyInfo(AppConstants.DEFAULT_USER_ID));
    }
}
