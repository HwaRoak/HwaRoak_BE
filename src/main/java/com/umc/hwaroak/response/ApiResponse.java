package com.umc.hwaroak.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final String status;  // 예: OK, BAD_REQUEST
    private final String code;
    private final String message;
    private final T data;

    // 내부에서 status 추출
    public static <T> ApiResponse<T> success(SuccessCode code, T data) {
        return new ApiResponse<>(code.getHttpStatus().name(), code.getCode(), code.getMessage(), data);
    }

    public static ApiResponse<Void> success(SuccessCode code) {
        return new ApiResponse<>(code.getHttpStatus().name(), code.getCode(), code.getMessage(), null);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getHttpStatus().name(), errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.getHttpStatus().name(), errorCode.getCode(), errorCode.getMessage(), data);
    }
}