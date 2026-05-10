package com.ecosync.api.dto.response;

import com.ecosync.application.port.in.GetSubscriptionUseCase;

import java.util.List;

public record GetSubscriptionResponse(Long id, String email, List<String> countryCodes, String calendarUrl) {

    public static GetSubscriptionResponse from(GetSubscriptionUseCase.Result result) {
        return new GetSubscriptionResponse(result.id(), result.email(), result.countryCodes(), result.calendarUrl());
    }
}
