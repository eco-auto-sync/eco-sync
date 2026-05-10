package com.ecosync.api.dto.response;

import com.ecosync.application.port.in.UpdateSubscriptionUseCase;

public record UpdateSubscriptionResponse(Long id, String calendarUrl) {

    public static UpdateSubscriptionResponse from(UpdateSubscriptionUseCase.Result result) {
        return new UpdateSubscriptionResponse(result.id(), result.calendarUrl());
    }
}
