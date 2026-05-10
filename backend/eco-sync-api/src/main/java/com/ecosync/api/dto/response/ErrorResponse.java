package com.ecosync.api.dto.response;

import com.ecosync.application.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "에러 코드", example = "SUBSCRIPTION_001") String code,
        @Schema(description = "에러 메시지", example = "구독 정보를 찾을 수 없습니다.") String message
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message);
    }
}
