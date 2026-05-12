package com.ecosync.api.dto.response;

import com.ecosync.application.port.in.GetSubscriptionUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetSubscriptionResponse(
        @Schema(description = "구독 ID", example = "1") Long id,
        @Schema(description = "구독자 이메일", example = "user@example.com") String email,
        @Schema(description = "구독 중인 국가 코드 목록", example = "[\"KR\", \"US\"]") List<String> countryCodes,
        @Schema(description = "ICS 구독 URL", example = "https://ecosync.com/api/calendar/abc123-uuid/subscribe") String calendarUrl
) {

    public static GetSubscriptionResponse from(GetSubscriptionUseCase.Result result) {
        return new GetSubscriptionResponse(result.id(), result.email(), result.countryCodes(), result.calendarUrl());
    }
}
