package com.umc.hwaroak.response;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode implements BaseCode{

    TEST_ERROR(HttpStatus.BAD_REQUEST, "TEST", "오류 응답에 대한 테스트입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ME4001", "회원을 찾을 수 없습니다."),

    // Diary
    DIARY_ALREADY_RECORDED(HttpStatus.BAD_REQUEST, "DE4001", "일기는 하루만 기록할 수 있습니다."),
    DIARY_NOT_FOUND(HttpStatus.BAD_REQUEST, "DE4002", "해당 일기 ID가 존재하지 않습니다."),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "메소드 요청이 잘못됐습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SE5001", "서버 내의 오류입니다."),

    // Notice 관련
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NO4041", "해당 공지를 찾을 수 없습니다."),

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
