package com.ecosync.application.port.out;

import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPort {
    Optional<Subscription> findByEmail(String email);
    Optional<Subscription> findByCalendarToken(String calendarToken);
    Subscription save(Subscription subscription);
    void saveInterests(List<SubscriptionInterest> interests);
    void softDeleteInterestsBySubscriptionId(Long subscriptionId);
    List<SubscriptionInterest> findActiveInterestsBySubscriptionId(Long subscriptionId);
}
