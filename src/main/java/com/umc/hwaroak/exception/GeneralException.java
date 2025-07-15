package com.umc.hwaroak.exception;

import com.umc.hwaroak.response.ErrorCode;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final ErrorCode errorCode;

    public GeneralException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
