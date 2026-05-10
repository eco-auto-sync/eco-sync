package com.ecosync.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateSubscriptionRequest(
        @Schema(description = "구독자 이메일", example = "user@example.com")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotEmpty(message = "이메일을 입력해주세요.")
        String email,

        @Schema(description = "구독할 국가 코드 목록 (1개 이상)", example = "[\"KR\", \"US\"]")
        @NotEmpty(message = "국가를 1개 이상 선택해주세요.")
        List<String> countryCodes
) {}
