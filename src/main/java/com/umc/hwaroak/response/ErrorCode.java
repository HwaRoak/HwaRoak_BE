package com.umc.hwaroak.response;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode implements BaseCode{

    TEST_ERROR(HttpStatus.BAD_REQUEST, "TEST", "오류 응답에 대한 테스트입니다."),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "메소드 요청이 잘못됐습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SE5001", "서버 내의 오류입니다."),

    // Notice 관련
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NO4041", "해당 공지를 찾을 수 없습니다."),


    //Member
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),

    // login
    INVALID_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 access token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"AUTH_002","유효하지 않은 refresh token입니다.")

    // Friend
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4041", "해당 친구 요청을 찾을 수 없습니다."),
    FRIEND_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, "FRIEND4002", "이미 처리된 친구 요청입니다."),
    CANNOT_ADD_SELF(HttpStatus.BAD_REQUEST, "FRIEND4003", "자기 자신에게는 친구 요청을 보낼 수 없습니다."),
    FRIEND_ALREADY_EXISTS_OR_REQUESTED(HttpStatus.BAD_REQUEST, "FRIEND4004", "이미 친구이거나 요청을 보낸 상태입니다."),
    FRIEND_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "FRIEND4005", "해당 친구 상태에서는 삭제할 수 없습니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4042", "해당 친구 관계를 찾을 수 없습니다."),

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
