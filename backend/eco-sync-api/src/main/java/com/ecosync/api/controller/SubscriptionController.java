package com.ecosync.api.controller;

import com.ecosync.api.dto.request.CreateSubscriptionRequest;
import com.ecosync.api.dto.response.CreateSubscriptionResponse;
import com.ecosync.application.port.in.CreateSubscriptionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscriptions", description = "구독 관리")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final CreateSubscriptionUseCase createSubscriptionUseCase;

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
}
