package com.ecosync.api.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateSubscriptionRequest(
        @NotEmpty(message = "국가 코드는 1개 이상이어야 합니다.")
        List<String> countryCodes
) {}
