package com.umc.hwaroak.response;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode implements BaseCode{

    TEST_ERROR(HttpStatus.BAD_REQUEST, "TEST", "오류 응답에 대한 테스트입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ME4001", "회원을 찾을 수 없습니다."),

    // Diary
    DIARY_ALREADY_RECORDED(HttpStatus.BAD_REQUEST, "DE4001", "일기는 하루만 기록할 수 있습니다."),
    DIARY_NOT_FOUND(HttpStatus.BAD_REQUEST, "DE4002", "해당 일기 ID가 존재하지 않습니다."),

    // emotion
    TOO_MANY_EMOTIONS(HttpStatus.BAD_REQUEST, "DE4003", "저장할 수 있는 감정의 최대 개수는 3개입니다."),
    EMOTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "DE4004", "해당 감정을 찾을 수 없습니다."),
    FAILED_EMOTION_PARSING(HttpStatus.BAD_REQUEST, "DE4005", "감정 저장 처리에 실패하였습니다."),

    // Auth
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"AUTH_002","유효하지 않은 refresh token입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "메소드 요청이 잘못됐습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "접근 권한이 없습니다."),

    // Notice 관련
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NO4041", "해당 공지를 찾을 수 없습니다."),


    // JWT token
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT_001", "서명이 유효하지 않습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_002", "토큰이 만료되었습니다."),
    MALFORMED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT_003", "토큰 형식이 잘못되었습니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT_004", "지원되지 않는 토큰입니다."),
    EMPTY_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT_005", "토큰이 비어있거나 잘못되었습니다."),


    // Friend
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4041", "해당 친구 요청을 찾을 수 없습니다."),
    FRIEND_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, "FRIEND4002", "이미 처리된 친구 요청입니다."),
    CANNOT_ADD_SELF(HttpStatus.BAD_REQUEST, "FRIEND4003", "자기 자신에게는 친구 요청을 보낼 수 없습니다."),
    FRIEND_ALREADY_EXISTS_OR_REQUESTED(HttpStatus.BAD_REQUEST, "FRIEND4004", "이미 친구이거나 요청을 보낸 상태입니다."),
    FRIEND_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "FRIEND4005", "해당 친구 상태에서는 삭제할 수 없습니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4042", "해당 친구 관계를 찾을 수 없습니다."),

    // SERVER
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SE5001", "서버 내의 오류입니다."),
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
