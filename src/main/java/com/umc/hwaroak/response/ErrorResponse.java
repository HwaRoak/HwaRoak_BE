package com.umc.hwaroak.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Builder
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final HttpStatus status;
    private final String code;
    private final String message;

    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getHttpStatus())
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
                );
    }
}
