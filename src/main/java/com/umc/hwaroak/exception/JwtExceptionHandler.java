package com.umc.hwaroak.exception;

import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
@RestControllerAdvice
public class JwtExceptionHandler {

    @ExceptionHandler({SecurityException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleInvalidSignature(Exception ex) {
        return ErrorResponse.of(ErrorCode.INVALID_JWT_SIGNATURE);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        return ErrorResponse.of(ErrorCode.EXPIRED_JWT_TOKEN);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwt(MalformedJwtException ex) {
        return ErrorResponse.of(ErrorCode.MALFORMED_JWT_TOKEN);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedJwt(UnsupportedJwtException ex) {
        return ErrorResponse.of(ErrorCode.UNSUPPORTED_JWT_TOKEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleEmptyJwt(IllegalArgumentException ex) {
        return ErrorResponse.of(ErrorCode.EMPTY_JWT_TOKEN);
    }
}