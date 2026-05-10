package com.ecosync.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateSubscriptionRequest(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotEmpty(message = "이메일을 입력해주세요.")
        String email,

        @NotEmpty(message = "국가를 1개 이상 선택해주세요.")
        List<String> countryCodes
) {}
