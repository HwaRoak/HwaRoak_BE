package com.umc.hwaroak.exception;

import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    protected ResponseEntity<ErrorResponse> handlerGeneralException(GeneralException e, HttpServletRequest request) {
        logError(e, request);
        return ErrorResponse.of(e.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logError(ex, request);
        return ErrorResponse.of(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logError(ex, request);
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error(errorMessage);
        return ErrorResponse.of(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlerException(Exception e, HttpServletRequest request) {
        logError(e, request);
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void logError(Exception e, HttpServletRequest request) {
        log.error("Request URI : [{}] {}", request.getMethod(), request.getRequestURI());
        log.error("Exception : ", e);
    }
}