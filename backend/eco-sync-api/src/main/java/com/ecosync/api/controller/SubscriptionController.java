package com.ecosync.api.controller;

import com.ecosync.api.dto.request.CreateSubscriptionRequest;
import com.ecosync.api.dto.request.UpdateSubscriptionRequest;
import com.ecosync.api.dto.response.CreateSubscriptionResponse;
import com.ecosync.api.dto.response.GetSubscriptionResponse;
import com.ecosync.api.dto.response.UpdateSubscriptionResponse;
import com.ecosync.application.port.in.CreateSubscriptionUseCase;
import com.ecosync.application.port.in.GetSubscriptionUseCase;
import com.ecosync.application.port.in.UpdateSubscriptionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscriptions", description = "구독 관리")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Validated
public class SubscriptionController {

    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final UpdateSubscriptionUseCase updateSubscriptionUseCase;

    @Operation(summary = "구독 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSubscriptionResponse create(@Valid @RequestBody CreateSubscriptionRequest request) {
        CreateSubscriptionUseCase.Command command = new CreateSubscriptionUseCase.Command(
                request.email(),
                request.countryCodes()
        );
        return CreateSubscriptionResponse.from(createSubscriptionUseCase.create(command));
    }

    @Operation(summary = "구독 수정")
    @PutMapping("/{id}")
    public UpdateSubscriptionResponse update(@PathVariable Long id,
                                             @Valid @RequestBody UpdateSubscriptionRequest request) {
        UpdateSubscriptionUseCase.Command command = new UpdateSubscriptionUseCase.Command(id, request.countryCodes());
        return UpdateSubscriptionResponse.from(updateSubscriptionUseCase.update(command));
    }

    @Operation(summary = "구독 재조회")
    @GetMapping
    public GetSubscriptionResponse getByEmail(
            @RequestParam @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email) {
        return GetSubscriptionResponse.from(getSubscriptionUseCase.getByEmail(new GetSubscriptionUseCase.Query(email)));
    }
}
