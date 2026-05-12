package com.ecosync.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateSubscriptionRequest(
        @Schema(description = "변경할 국가 코드 목록 (1개 이상)", example = "[\"KR\", \"JP\", \"US\"]")
        @NotEmpty(message = "국가 코드는 1개 이상이어야 합니다.")
        List<String> countryCodes
) {}
