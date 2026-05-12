package com.ecosync.api.dto.response;

import com.ecosync.application.port.in.CreateSubscriptionUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateSubscriptionResponse(
        @Schema(description = "구독 ID", example = "1") Long id,
        @Schema(description = "ICS 구독 URL", example = "https://ecosync.com/api/calendar/abc123-uuid/subscribe") String calendarUrl
) {

    public static CreateSubscriptionResponse from(CreateSubscriptionUseCase.Result result) {
        return new CreateSubscriptionResponse(result.id(), result.calendarUrl());
    }
}
