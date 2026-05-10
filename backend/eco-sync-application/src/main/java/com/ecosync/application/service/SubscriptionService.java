package com.ecosync.application.service;

import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.application.port.in.CreateSubscriptionUseCase;
import com.ecosync.application.port.in.GetCountriesUseCase;
import com.ecosync.application.port.in.GetSubscriptionUseCase;
import com.ecosync.application.port.out.SubscriptionPort;
import com.ecosync.domain.subscription.Country;
import com.ecosync.domain.subscription.InterestType;
import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService implements CreateSubscriptionUseCase, GetSubscriptionUseCase {

    private final SubscriptionPort subscriptionPort;
    private final GetCountriesUseCase getCountriesUseCase;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public CreateSubscriptionUseCase.Result create(CreateSubscriptionUseCase.Command command) {
        validateCountryCodes(command.countryCodes());

        Optional<Subscription> existing = subscriptionPort.findByEmail(command.email());

        Subscription subscription;
        if (existing.isPresent()) {
            Subscription found = existing.get();
            if (found.isActive()) {
                throw new EcoSyncException(ErrorCode.DUPLICATE_SUBSCRIPTION);
            }
            found.restore();
            subscription = subscriptionPort.save(found);
        } else {
            subscription = subscriptionPort.save(Subscription.create(command.email()));
        }

        subscriptionPort.softDeleteInterestsBySubscriptionId(subscription.getId());

        Long subscriptionId = subscription.getId();
        List<SubscriptionInterest> interests = command.countryCodes().stream()
                .map(code -> {
                    SubscriptionInterest interest = SubscriptionInterest.builder()
                            .subscriptionId(subscriptionId)
                            .interestType(InterestType.COUNTRY)
                            .interestValue(code)
                            .build();
                    return interest;
                })
                .toList();
        subscriptionPort.saveInterests(interests);

        return new CreateSubscriptionUseCase.Result(subscription.getId(), buildCalendarUrl(subscription));
    }

    @Override
    @Transactional(readOnly = true)
    public GetSubscriptionUseCase.Result getByEmail(GetSubscriptionUseCase.Query query) {
        Subscription subscription = subscriptionPort.findByEmail(query.email())
                .filter(Subscription::isActive)
                .orElseThrow(() -> new EcoSyncException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        List<String> countryCodes = subscriptionPort.findActiveInterestsBySubscriptionId(subscription.getId())
                .stream()
                .map(SubscriptionInterest::getInterestValue)
                .toList();

        return new GetSubscriptionUseCase.Result(subscription.getId(), subscription.getEmail(), countryCodes, buildCalendarUrl(subscription));
    }

    private String buildCalendarUrl(Subscription subscription) {
        return baseUrl + "/api/calendar/" + subscription.getCalendarToken() + "/subscribe";
    }

    private void validateCountryCodes(List<String> countryCodes) {
        Set<String> validCodes = getCountriesUseCase.getCountries().stream()
                .map(Country::code)
                .collect(Collectors.toSet());

        List<String> invalid = countryCodes.stream()
                .filter(code -> !validCodes.contains(code))
                .toList();

        if (!invalid.isEmpty()) {
            throw new EcoSyncException(ErrorCode.UNSUPPORTED_COUNTRY);
        }
    }
}
