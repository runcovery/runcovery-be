package com.likelion14.runcovery.user;

import com.likelion14.runcovery.common.AppConstants;
import com.likelion14.runcovery.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "유저/등록", tags = "1. User")
    @PostMapping
    public ResponseEntity<ApiResponse<UserCreateResponseDto>> createUser(
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        UserCreateResponseDto response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    @Operation(summary = "유저/조회", tags = "1. User")
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo() {
        return ApiResponse.ok(userService.getMyInfo(AppConstants.DEFAULT_USER_ID));
    }

    @Operation(summary = "유저/마이페이지", tags = "1. User")
    @GetMapping("/mypage")
    public ApiResponse<MyStatsResponseDto> getMyStats() {
        return ApiResponse.ok(userService.getMyStats());
    }
}
