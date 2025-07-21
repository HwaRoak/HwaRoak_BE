package com.umc.hwaroak.exception;

import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.security.SignatureException;
import io.jsonwebtoken.security.SecurityException;

@Slf4j // 자동으로 Logger 객체를 생성
@RestControllerAdvice
@Hidden
public class JwtExceptionHandler {

    @ExceptionHandler({SecurityException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleInvalidSignature(Exception ex) {
        log.warn("유효하지 않은 JWT 서명입니다: {}",ex.getMessage(), ex);
        return ErrorResponse.of(ErrorCode.INVALID_JWT_SIGNATURE);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        log.warn("만료된 JWT 토큰입니다: {}",ex.getMessage(), ex);
        return ErrorResponse.of(ErrorCode.EXPIRED_JWT_TOKEN);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwt(MalformedJwtException ex) {
        log.warn("손상된 JWT 토큰입니다: {}",ex.getMessage(), ex);
        return ErrorResponse.of(ErrorCode.MALFORMED_JWT_TOKEN);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedJwt(UnsupportedJwtException ex) {
        log.warn("지원하지 않는 JWT 토큰입니다: {}",ex.getMessage(), ex);
        return ErrorResponse.of(ErrorCode.UNSUPPORTED_JWT_TOKEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleEmptyJwt(IllegalArgumentException ex) {
        log.warn("비어있거나 잘못된 JWT 토큰입니다: {}",ex.getMessage(), ex);
        return ErrorResponse.of(ErrorCode.EMPTY_JWT_TOKEN);
    }
}