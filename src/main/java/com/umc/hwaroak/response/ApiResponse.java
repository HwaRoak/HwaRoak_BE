package com.umc.hwaroak.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final String status;  // OK, CREATED 등
    private final String code;
    private final String message;
    private final T data;

    public static ApiResponse<?> of(SuccessCode code) {
        return new ApiResponse<>(code.getHttpStatus().name(), code.getCode(), code.getMessage(), null);
    }

    public static <T> ApiResponse<?> of(SuccessCode code, T data) {
        return new ApiResponse<>(code.getHttpStatus().name(), code.getCode(), code.getMessage(), data);
    }
}