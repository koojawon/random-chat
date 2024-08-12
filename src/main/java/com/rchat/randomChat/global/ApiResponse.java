package com.rchat.randomChat.global;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {

    private static final String SUCCESS_STATE = "SUCCESS";
    private static final String FAIL_STATE = "FAIL";
    private static final String ERROR_STATE = "ERROR";

    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> createSuccess(String message, T data) {
        return new ApiResponse<>(SUCCESS_STATE, message, data);
    }

    public static <T> ApiResponse<T> createFail(String message, T data) {
        return new ApiResponse<>(FAIL_STATE, message, data);
    }

    public static ApiResponse<?> createError(String message) {
        return new ApiResponse<>(ERROR_STATE, message, null);
    }
}
