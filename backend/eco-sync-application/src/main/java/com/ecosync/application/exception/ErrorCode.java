package com.ecosync.application.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION_001", "구독 정보를 찾을 수 없습니다."),
    DUPLICATE_SUBSCRIPTION(HttpStatus.CONFLICT, "SUBSCRIPTION_002", "이미 구독 중인 이메일입니다."),

    // Calendar
    CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR_001", "캘린더를 찾을 수 없습니다."),
    CALENDAR_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CALENDAR_002", "ICS 파일 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
