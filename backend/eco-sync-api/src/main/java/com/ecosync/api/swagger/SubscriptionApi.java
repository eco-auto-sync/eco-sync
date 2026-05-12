package com.ecosync.api.swagger;

import com.ecosync.api.dto.request.CreateSubscriptionRequest;
import com.ecosync.api.dto.request.UpdateSubscriptionRequest;
import com.ecosync.api.dto.response.CreateSubscriptionResponse;
import com.ecosync.api.dto.response.GetSubscriptionResponse;
import com.ecosync.api.dto.response.UpdateSubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Tag(name = "Subscriptions", description = "구독 관리")
public interface SubscriptionApi {

    @Operation(summary = "구독 생성")
    CreateSubscriptionResponse create(@Valid CreateSubscriptionRequest request);

    @Operation(summary = "구독 수정")
    UpdateSubscriptionResponse update(
            @Parameter(description = "구독 ID", example = "1") Long id,
            @Valid UpdateSubscriptionRequest request);

    @Operation(summary = "구독 취소")
    void cancel(
            @Parameter(description = "구독 ID", example = "1") Long id);

    @Operation(summary = "구독 재조회")
    GetSubscriptionResponse getByEmail(
            @Parameter(description = "구독자 이메일", example = "user@example.com")
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email);
}
