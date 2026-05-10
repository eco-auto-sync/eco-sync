package com.ecosync.api.dto.response;

import com.ecosync.application.port.in.UpdateSubscriptionUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateSubscriptionResponse(
        @Schema(description = "구독 ID", example = "1") Long id,
        @Schema(description = "ICS 구독 URL", example = "https://ecosync.com/api/calendar/abc123-uuid/subscribe") String calendarUrl
) {

    public static UpdateSubscriptionResponse from(UpdateSubscriptionUseCase.Result result) {
        return new UpdateSubscriptionResponse(result.id(), result.calendarUrl());
    }
}
