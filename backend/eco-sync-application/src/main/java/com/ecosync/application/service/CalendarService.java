package com.ecosync.application.service;

import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.application.port.in.GetCalendarUseCase;
import com.ecosync.application.port.in.GetCountriesUseCase;
import com.ecosync.application.port.out.EconomicEventPort;
import com.ecosync.application.port.out.IcsGeneratorPort;
import com.ecosync.application.port.out.SubscriptionPort;
import com.ecosync.domain.event.EconomicEvent;
import com.ecosync.domain.subscription.Country;
import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalendarService implements GetCalendarUseCase {

    private final SubscriptionPort subscriptionPort;
    private final EconomicEventPort economicEventPort;
    private final GetCountriesUseCase getCountriesUseCase;
    private final IcsGeneratorPort icsGeneratorPort;

    @Override
    public Result getCalendar(String token) {
        Subscription subscription = subscriptionPort.findByCalendarToken(token)
                .filter(Subscription::isActive)
                .orElseThrow(() -> new EcoSyncException(ErrorCode.CALENDAR_NOT_FOUND));

        Set<String> countryCodes = subscriptionPort.findActiveInterestsBySubscriptionId(subscription.getId())
                .stream()
                .map(SubscriptionInterest::getInterestValue)
                .collect(Collectors.toSet());

        List<String> exchanges = getCountriesUseCase.getCountries().stream()
                .filter(c -> countryCodes.contains(c.code()))
                .flatMap(c -> Arrays.stream(c.exchange().split(",")).map(String::trim))
                .toList();

        List<EconomicEvent> events = economicEventPort.findHolidaysByExchanges(exchanges);

        return new Result(icsGeneratorPort.generate(events));
    }
}
