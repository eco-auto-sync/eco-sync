package com.ecosync.api.dto.response;

import com.ecosync.application.port.in.CreateSubscriptionUseCase;

public record CreateSubscriptionResponse(Long id, String calendarUrl) {

    public static CreateSubscriptionResponse from(CreateSubscriptionUseCase.Result result) {
        return new CreateSubscriptionResponse(result.id(), result.calendarUrl());
    }
}
