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

    // 실패
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, false, null, message);
    }
}
