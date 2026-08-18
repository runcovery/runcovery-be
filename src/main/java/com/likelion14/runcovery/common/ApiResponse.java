package com.likelion14.runcovery.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private boolean success;
    private T data;
    private String message;

    // 성공 + 데이터
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), true, data, null);
    }

    // 성공 + 데이터 없음 + 메시지
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(HttpStatus.OK.value(), true, null, message);
    }

    // 성공 + 데이터 + 메시지
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(HttpStatus.OK.value(), true, data, message);
    }

    // 성공 + 데이터 + 상태코드 (200이 아닌 성공 응답, 예: 201 Created)
    public static <T> ApiResponse<T> ok(T data, HttpStatus status) {
        return new ApiResponse<>(status.value(), true, data, null);
    }

    // 실패
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, false, null, message);
    }
}
