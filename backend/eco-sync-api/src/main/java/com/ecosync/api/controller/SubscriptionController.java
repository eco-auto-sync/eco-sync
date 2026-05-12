package com.ecosync.api.controller;

import com.ecosync.api.dto.request.CreateSubscriptionRequest;
import com.ecosync.api.dto.request.UpdateSubscriptionRequest;
import com.ecosync.api.dto.response.CreateSubscriptionResponse;
import com.ecosync.api.dto.response.GetSubscriptionResponse;
import com.ecosync.api.dto.response.UpdateSubscriptionResponse;
import com.ecosync.api.swagger.SubscriptionApi;
import com.ecosync.application.port.in.CancelSubscriptionUseCase;
import com.ecosync.application.port.in.CreateSubscriptionUseCase;
import com.ecosync.application.port.in.GetSubscriptionUseCase;
import com.ecosync.application.port.in.UpdateSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Validated
public class SubscriptionController implements SubscriptionApi {

    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final UpdateSubscriptionUseCase updateSubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSubscriptionResponse create(@RequestBody CreateSubscriptionRequest request) {
        CreateSubscriptionUseCase.Command command = new CreateSubscriptionUseCase.Command(
                request.email(),
                request.countryCodes()
        );
        return CreateSubscriptionResponse.from(createSubscriptionUseCase.create(command));
    }

    @Override
    @PutMapping("/{id}")
    public UpdateSubscriptionResponse update(@PathVariable Long id,
                                             @RequestBody UpdateSubscriptionRequest request) {
        UpdateSubscriptionUseCase.Command command = new UpdateSubscriptionUseCase.Command(id, request.countryCodes());
        return UpdateSubscriptionResponse.from(updateSubscriptionUseCase.update(command));
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        cancelSubscriptionUseCase.cancel(id);
    }

    @Override
    @GetMapping
    public GetSubscriptionResponse getByEmail(@RequestParam String email) {
        return GetSubscriptionResponse.from(getSubscriptionUseCase.getByEmail(new GetSubscriptionUseCase.Query(email)));
    }
}
